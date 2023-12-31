
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.helper.MessageCaptionFormatter
import dev.rasul.rentasybot.models.AdFeature
import dev.rasul.rentasybot.models.AdMessage
import dev.rasul.rentasybot.models.UserInfo
import dev.rasul.rentasybot.parsers.AdParser
import dev.rasul.rentasybot.queue.AdsMessageQueue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger


class OtodomParser(
    private val configDao: UserConfigDao,
    private val resources: Resources,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val messageQueue: AdsMessageQueue,
    private val messageCaptionFormatter: MessageCaptionFormatter
) : AdParser {

    companion object {
        private val OTODOM_BASE_URL = "https://www.otodom.pl/pl/wyniki/wynajem/mieszkanie/wiele-lokalizacji?"

        const val ADS_COUNT = 5
    }
    private val headers = hashMapOf(
        "authority" to "www.otodom.pl",
        "Host" to "www.otodom.pl",
        "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "user-agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
        "Content-Type" to "text/html; charset=utf-8",
        "Cookie" to "lang=pl"
    )


    private val rooms = hashMapOf(
        0 to "MORE",
        1 to "ONE",
        2 to "TWO",
        3 to "THREE",
        4 to "FOUR",
        5 to "FIVE",
        6 to "SIX",
        7 to "SEVEN",
        8 to "EIGHT",
        9 to "NINE",
        10 to "TEN"
    )

    override suspend fun startParsing(config: UserInfo) {
        Logger.getLogger(OkHttpClient::class.java.name).setLevel(Level.FINE)
        try {
            config.criteria?.let { criteria ->

                val city = resources.locations.firstOrNull { it.id == criteria.city }

                val filtered = rooms.filterKeys { it in criteria.roomMin!!..criteria.roomMax!! }

                val userDistricts = city?.locations
                    ?.filter { criteria.district.contains(it.id) }
                    ?.map { it.otodom.location }

                val params = mapOf(
                    "distanceRadius" to "0",
                    "page" to "1",
                    "limit" to "36",
                    "areaMin" to criteria.areaMin.toString(),
                    "areaMax" to criteria.areaMax.toString(),
                    "priceMin" to criteria.priceMin.toString(),
                    "priceMax" to criteria.priceMax.toString(),
                    "roomsNumber" to "[${filtered.values.joinToString(",")}]",
                    "by" to "DEFAULT",
                    "direction" to "DESC",
                    "viewType" to "listing"
                ).toMutableMap()


                if (criteria.allDistricts.not()) {
                    params["locations"] = "[${userDistricts?.joinToString(",")}]"
                }

                params["isPrivateOwner"] = criteria.onlyFromOwners.toString()


                val urlBuilder = OTODOM_BASE_URL.toHttpUrl()
                    .newBuilder()
                    .apply {
                        params.forEach { (key, value) ->
                            addQueryParameter(key, value)
                        }
                    }.build()

                val request = Request.Builder()
                    .url(urlBuilder)
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()

                println(response.request.url)

                val seenIds = config.seenOtodomIds.toMutableList()


                val document = Jsoup.connect(request.url.toString()).get()


                val scriptData = document.select("script").firstOrNull {
                    it.attr("id") == "__NEXT_DATA__"
                }?.let {
                    gson.fromJson(it.data(), JsonObject::class.java)
                }

                scriptData?.let {
                    val ads = scriptData["props"]
                        .asJsonObject["pageProps"]
                        .asJsonObject["data"]
                        .asJsonObject["searchAds"]
                        .asJsonObject["items"]
                        .asJsonArray


                    ads.take(ADS_COUNT).forEach { ad ->
                        val adId = ad.asJsonObject["id"].asString

                        val slug = ad.asJsonObject["slug"].asString
                        if (seenIds.contains(adId)) {
                            return@forEach
                        }

                        seenIds.add(adId)

                        configDao.updateUser(
                            config.telegramUserId,
                            params = mapOf(
                                UserInfo::seenOtodomIds.name to seenIds
                            )
                        )

//                    configDao.updateUser(newUserConfig)

                        val adUrl = "https://www.otodom.pl/pl/oferta/$slug"
                        parseAndSendResults(
                            chatId = config.telegramUserId,
                            lang = config.lang,
                            url = adUrl
                        )
                    }
                }
                response.close()
            }
        } catch (e: Exception) {

            // 2023-10-22T10:48:27+02:00
            // 2023-10-26T21:29:49+02:00
            e.printStackTrace()
        }
    }

    private fun parseAndSendResults(
        chatId: Long,
        lang: String,
        url: String
    ) {
        val document = Jsoup.connect(url)
            .headers(headers)
            .get()




        val scriptData = document.select("script").firstOrNull {
            it.attr("id") == "__NEXT_DATA__"
        }?.let {
            gson.fromJson(it.data(), JsonObject::class.java)
        }

        scriptData?.let {
            val ad = scriptData.getAsJsonObject("props")
                .getAsJsonObject("pageProps")
                .getAsJsonObject("ad")

            val title = ad.get("title").asString


            val city = ad["location"].asJsonObject["address"].asJsonObject["city"].asJsonObject["name"].asString
            val district = ad["location"].asJsonObject["address"].asJsonObject["district"].asJsonObject["name"].asString

            val attachments = mutableListOf<File>()

            val images = ad.getAsJsonArray("images")
            for (i in 0 until minOf(images.size(), 10)) {
                val image = images.get(i).asJsonObject
                val imageUrl = image.get("large").asString
                val imageFile = File.createTempFile("image", ".jpg")


                val imageResponse =
                    okHttpClient.newCall(Request.Builder().url(imageUrl.toHttpUrl()).build())
                        .execute()
                imageResponse.body?.bytes()?.let {
                    imageFile.writeBytes(it)
                    attachments.add(imageFile)
                }

            }

            val characteristics = ad.getAsJsonArray("characteristics").map { it.asJsonObject }

            val adFeature = AdFeature(
                title = title,
                city = city,
                district = district,
                room = characteristics.firstOrNull { it.get("key").asString == "rooms_num" }?.get("value")?.asString,
                floor = characteristics.firstOrNull { it.get("key").asString == "floor_no" }
                    ?.get("localizedValue")?.asString,
                price = characteristics.firstOrNull { it.get("key").asString == "price" }?.get("value")?.asString,
                administrativeRent =
                characteristics.firstOrNull { it.get("key").asString == "rent" }?.get("value")?.asString,
                deposit = characteristics.firstOrNull { it.get("key").asString == "deposit" }?.get("value")?.asString,
                area = characteristics.firstOrNull {
                    it.get("key").asString == "m"
                }?.get("value")?.asString,
                url = url,
                createdAt = ad["modifiedAt"].asString
            )
            messageQueue.addToStack(
                adMessage = AdMessage(
                    chatId = chatId,
                    text = messageCaptionFormatter.formatMessage(lang, adFeature),
                    attachments = attachments
                )
            )
        }
    }
}