package dev.rasul.rentasybot.parsers

import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.models.AdFeature
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MessageCaptionFormatter(private val resources: Resources) {

    private val decimalFormat = DecimalFormat("#,###.##").apply {
        decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ' '
        }
    }

    fun formatMessage(lang: String, feature: AdFeature): String {

        var price = "💵 <b>${resources.translates[lang].asJsonObject["price"].asString}</b> - ${
            decimalFormat.format(feature.price?.toFloat())
        } zł"

        feature.administrativeRent?.takeIf { it.isNotEmpty() }?.let { rent ->
            price += " (+${decimalFormat.format(rent.toFloat())} zł ${resources.translates[lang].asJsonObject["administrative_rent"].asString})".lowercase(
                Locale.getDefault()
            )
        }

        var message = """
        🏙️ <b>${resources.translates[lang].asJsonObject["city"].asString}</b> - ${feature.city}
        📍 <b>${resources.translates[lang].asJsonObject["district"].asString}</b> - ${feature.district}
        🛏 <b>${resources.translates[lang].asJsonObject["room"].asString}</b> - ${feature.room}
        📐 <b>${resources.translates[lang].asJsonObject["area"].asString}</b> - ${feature.area} ${if (lang == "ru" || lang == "ua") "м²" else "m²"}
        $price
    """.trimIndent()

        feature.deposit?.takeIf { it.isNotEmpty() }?.let { deposit ->
            message += "\n🔐 <b>${resources.translates[lang].asJsonObject["deposit"]}</b> - $deposit zł"
        }

        message += "\n\n<a href=\"${feature.url}\">${resources.translates[lang].asJsonObject["go_to_ad"].asString}</a>"
        return message
    }
}