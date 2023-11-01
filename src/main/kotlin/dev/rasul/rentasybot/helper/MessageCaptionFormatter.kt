package dev.rasul.rentasybot.helper

import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.models.AdFeature
import dev.rasul.rentasybot.models.UserInfo
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class MessageCaptionFormatter(private val resources: Resources) {


    private val decimalFormat = DecimalFormat("#,###.##").apply {
        decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ' '
        }
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")

    private val formatted = SimpleDateFormat("dd.MM.yyyy HH:mm")

    fun formatMessage(lang: String, feature: AdFeature): String {

        var price = "💵 <b>${resources.getTranslation(lang, "price")}</b> - ${
            decimalFormat.format(feature.price?.toFloat())
        } zł"

        feature.administrativeRent?.takeIf { it.isNotEmpty() }?.let { rent ->
            price += " (+${decimalFormat.format(rent.toFloat())} zł ${
                resources.getTranslation(
                    lang,
                    "administrative_rent"
                )
            })".lowercase(
                Locale.getDefault()
            )
        }


        val location = if (feature.district != null) {
            "${feature.district}, ${feature.city}"
        } else {
            "${feature.city}"
        }

        var message = """
        📍 <b>${resources.getTranslation(lang, "location")}</b> - $location
        🛏 <b>${resources.getTranslation(lang, "room")}</b> - ${feature.room}
        📐 <b>${
            resources.getTranslation(
                lang,
                "area"
            )
        }</b> - ${feature.area} ${if (lang == "ru" || lang == "ua") "м²" else "m²"}
        $price
       
    """.trimIndent()

        feature.deposit?.takeIf { it.isNotEmpty() }?.let { deposit ->
            message += "🔐 <b>${resources.getTranslation(lang, "deposit")}</b> - $deposit zł"
        }
        val formatted = sdf.parse(feature.createdAt)?.run {
            formatted.format(this)
        }
        message += """
            
            ------------------------------------
            📆 $formatted
        """.trimIndent()
        message += "\n\n<a href=\"${feature.url}\">${resources.getTranslation(lang, "go_to_ad")}</a>"
        return message
    }

    fun prepareConfirmationMessage(config: UserInfo): String {

        val userCriteria = config.criteria!!
        val lang = config.lang
        val city = resources.locations.firstOrNull { it.id == userCriteria.city }

        val locations = city?.let { _ ->
            city.locations
                .filter { location -> userCriteria.district.contains(location.id) }
                .map { location -> location.name }
        } ?: emptyList()

        val rooms = if (userCriteria.roomMin == userCriteria.roomMax) {
            userCriteria.roomMin.toString()
        } else {
            "${userCriteria.roomMin} - ${userCriteria.roomMax}"
        }

        val area =
            "${userCriteria.areaMin} - ${userCriteria.areaMax} ${if (lang == "ru" || lang == "ua") "м²" else "m²"}"

        val message = """
        <b>${resources.getTranslation(config.lang, "selected_criteria")}</b>
        
        🏙️ <b>${resources.getTranslation(config.lang, "city")}</b> - ${city?.name ?: ""}
        
        📍 <b>${resources.getTranslation(config.lang, "district")}</b> - ${
            if (userCriteria.allDistricts) resources.getTranslation(
                config.lang,
                "all_districts"
            ) else locations.joinToString(", ")
        }
        
        🛏 <b>${resources.getTranslation(config.lang, "room")}</b> - $rooms
        
        📐 <b>${resources.getTranslation(config.lang, "area")}</b> - $area
        
        💵 <b>${resources.getTranslation(config.lang, "price")}</b> - ${decimalFormat.format(userCriteria.priceMin)} - ${
            decimalFormat.format(
                userCriteria.priceMax
            )
        } PLN
        
        👨🏻‍💼 <b>${resources.getTranslation(config.lang, "ad_type")}</b> - ${
            if (userCriteria.onlyFromOwners) {
                resources.getTranslation(config.lang, "ad_type_private")
            } else {
                resources.getTranslation(config.lang, "ad_type_all")
            }
        }
        
    """.trimIndent()

        return message
    }
}