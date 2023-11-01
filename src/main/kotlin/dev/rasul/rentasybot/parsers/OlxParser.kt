
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.helper.MessageCaptionFormatter
import dev.rasul.rentasybot.models.*
import dev.rasul.rentasybot.parsers.AdParser
import dev.rasul.rentasybot.queue.AdsMessageQueue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class OlxParser(
    private val configDao: UserConfigDao,
    private val resources: Resources,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val messageQueue: AdsMessageQueue,
    private val messageCaptionFormatter: MessageCaptionFormatter
) : AdParser {

    companion object {
        const val ADS_COUNT = 5
    }


    private val rooms = mapOf(
        1 to "one",
        2 to "two",
        3 to "three",
        4 to "four"
    )

    private val headers = mapOf(
        "Host" to "www.olx.pl",
        "Accept" to "*/*",
        "Version" to "v1.17",
        "Accept-Encoding" to "gzip, deflate, br",
        "Accept-Language" to "pl",
        // "Authorization" to "Bearer 6210067c9a8c0d534aac102d4a16cad46506fd28",
        "X-Platform-Type" to "ios",
        "User-Agent" to "Android App Ver 4.73.0 (iOS 16.6)",
        "X-Device-Id" to "e7e2f376c6e2285a10be0a7465b04c691855a1f2",
        "Connection" to "keep-alive"
    )

    @Throws(Exception::class)
    override suspend fun startParsing(config: UserInfo) {
        config.criteria?.let { criteria ->

            val city = resources.locations.find { it.id == criteria.city }!!


            val params = mutableMapOf(
                "category_id" to 15,
                "city_id" to city.olx.id,
                "sort_by" to "created_at:desc",
                "filter_float_price:to" to criteria.priceMax,
                "filter_float_m:from" to criteria.areaMin,
                "filter_float_m:to" to criteria.areaMax,
                "filter_enum_rooms[]" to (criteria.roomMin!! until criteria.roomMax!!)
                    .filter { it <= 4 }
                    .mapNotNull { rooms[it] }
            )

            if (criteria.onlyFromOwners) {
                params["owner_type"] = "private"
            }

            val location = if (criteria.allDistricts.not()) {
                city.locations.firstOrNull()
//            val locations = city?.locations?.map {
//                criteria.district.contains(it.id)
//            }
//
//                (city?.get("locations") as? List<Map<String, Any>>)
//                ?.filter { it["id"] in criteria["district"] as? List<Int> ?: emptyList() }
//
//            locations?.forEach { location ->
//                params["district_id"] = location["olx"]?.get("id") as? Int
//                coros.addAll(getItemsTasks(params, location, location, userConfig))
//            }
            } else {
                null
            }

            val url = "https://www.olx.pl/api/v1/offers".toHttpUrl()
                .newBuilder()
                .apply {
                    params.forEach { (key, value) ->
                        if (value is List<*>) {
                            value.forEach {
                                addQueryParameter(key, it.toString())
                            }
                        } else {
                            addQueryParameter(key, value.toString())
                        }
                    }
                }.build()


            val request = Request.Builder()
                .url(url)
//                .apply {
//                    headers.forEach {
//                        addHeader(it.key,it.value)
//                    }
//                }
                .get()

            val response = okHttpClient.newCall(request.build()).execute().use {
                gson.fromJson(it.body?.string(), JsonObject::class.java)
            }


            val ads = response["data"].asJsonArray

            val seenIds = config.seenOtodomIds.toMutableList()


            ads.take(ADS_COUNT).forEach {
                val ad = it.asJsonObject
                if (ad == null || ad.has("partner")) {
                    println("Returned")
                    return@forEach
                }

                val id = ad["id"].asString

                seenIds.add(id)

                configDao.updateUser(
                    config.telegramUserId,
                    params = mapOf(
                        UserInfo::seenOlxIds.name to seenIds
                    )
                )

                parseAndSendResults(
                    id = id,
                    city = city,
                    location = location,
                    config = config
                )

            }
        }
    }


    private fun parseAndSendResults(
        id: String,
        city: Location,
        location: Location?,
        config: UserInfo
    ) {

        val request = Request.Builder()
            .url("https://www.olx.pl/api/v1/offers/$id".toHttpUrl())
            .build()


        val response = okHttpClient.newCall(request).execute().use {
            gson.fromJson(it.body?.string(), JsonObject::class.java).asJsonObject
        }

        val ad = response["data"].asJsonObject


        val title = ad["title"].asString

        var price: String? = null
        var administrativeRent: String? = null
        var room: String? = null
        var floor: String? = null
        var area: String? = null
        val url = ad.get("url").asString


        val cityName = city.name
        val district = location?.name


        val params = gson.fromJson<List<OlxParam>>(ad["params"], object : TypeToken<List<OlxParam>>() {}.type)

        params.forEach { feature ->
            when (feature.key) {
                "price" -> price = feature.value.value
                "rent" -> administrativeRent = feature.value.key
                "rooms" -> {
                    val rooms = rooms.entries.find {
                        it.value.equals(
                            feature.value.key,
                            ignoreCase = true
                        )
                    }
                    room = rooms?.key?.toString()
                }

                "floor_select" -> floor = feature.value.label
                "m" -> area = feature.value.key
            }
        }


        val attachments = mutableListOf<File>()
        ad["photos"].asJsonArray.take(10).forEach {
            val image = it.asJsonObject
            val imageFile = File.createTempFile("rentasy_", ".jpg")
            val imageLink = image["link"].asString
            val imageUrl = imageLink.replace(
                "{width}", image["width"].asString
            ).replace("{height}", image["height"].asString)

            val imageResponse =
                okHttpClient.newCall(
                    Request.Builder().url(imageUrl.toHttpUrl()).build()
                ).execute()
            imageResponse.body?.bytes()?.let { bytes ->
                imageFile.writeBytes(bytes)
                attachments.add(imageFile)
            }
        }


        val adFeature = AdFeature(
            title = title,
            city = cityName,
            district = district,
            room = room,
            floor = floor,
            price = price,
            administrativeRent = administrativeRent,
            area = area,
            url = url,
            createdAt = ad["last_refresh_time"].asString
        )


        messageQueue.addToStack(
            adMessage = AdMessage(
                chatId = config.telegramUserId,
                text = messageCaptionFormatter.formatMessage(config.lang, adFeature),
                attachments = attachments
            )
        )

    }
}