import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.handlers.Callbacks
import dev.rasul.rentasybot.models.UserCriteria


enum class RoomKeyboardType {
    Min,
    Max
}

class KeyboardHelper(
    private val resources: Resources,
) {


    fun getLangKeyboard(isEdit: Boolean): InlineKeyboardMarkup {
        val callback = if (isEdit) Callbacks.CALLBACK_CHANGE_LANG else Callbacks.CALLBACK_LANG

        val keyboardButtons = resources.languages.map { lang ->
            InlineKeyboardButton.CallbackData(
                text = lang.name, callbackData = "${callback}_${lang.id}"
            )
        }.chunked(2)

        return InlineKeyboardMarkup.create(keyboardButtons)
    }


    fun getCitiesKeyboard(): InlineKeyboardMarkup {
        val citiesButtons = resources.locations.map { city ->
            InlineKeyboardButton.CallbackData(
                text = city.name, callbackData = "${Callbacks.CALLBACK_CITY}_${city.id}"
            )
        }


        val mainCity = citiesButtons.first()

        return InlineKeyboardMarkup.create(buttons = citiesButtons.drop(1).chunked(2).toMutableList().apply {
            add(0, listOf(mainCity))
        })
    }

    fun getDistrictsKeyboard(lang: String, criteria: UserCriteria): InlineKeyboardMarkup {

        val city = resources.locations.firstOrNull { it.id == criteria.city }


        val actionButtons = listOf(
            InlineKeyboardButton.CallbackData(
                text = resources.getTranslation(
                    lang = lang, key = "clear_all_selection"
                ), callbackData = "${Callbacks.CALLBACK_DISTRICT}_clear"
            ), InlineKeyboardButton.CallbackData(
                text = resources.getTranslation(
                    lang = lang, key = "next"
                ), callbackData = "${Callbacks.CALLBACK_DISTRICT}_next"
            )
        )

        val districtButtons: List<List<InlineKeyboardButton>> = city?.locations?.map { district ->
            val text = if (criteria.district.contains(district.id) || criteria.allDistricts) {
                "🟢 ${district.name}"
            } else {
                "⚪ ${district.name}"
            }
            InlineKeyboardButton.CallbackData(
                text = text, callbackData = "${Callbacks.CALLBACK_DISTRICT}_${district.id}"
            )
        }?.chunked(2)?.toMutableList()?.also { buttons ->
            buttons.add(
                0, listOf(
                    InlineKeyboardButton.CallbackData(
                        text = resources.getTranslation(
                            lang = lang, key = "all_districts"
                        ), callbackData = "${Callbacks.CALLBACK_DISTRICT}_all"
                    )
                )
            )

            if (criteria.district.isNotEmpty()) {
                actionButtons.forEach {
                    buttons.add(listOf(it))
                }
            }

        } ?: emptyList()
        return InlineKeyboardMarkup.create(buttons = districtButtons)
    }


    fun getRoomKeyboard(type: RoomKeyboardType): InlineKeyboardMarkup {

        val callback = (if (type == RoomKeyboardType.Min) Callbacks.CALLBACK_ROOM_MIN else Callbacks.CALLBACK_ROOM_MAX)

        val buttons = (1 until 4).map { number ->
            InlineKeyboardButton.CallbackData(
                text = number.toString(),
                callbackData = "${callback}_$number"
            )
        }.toMutableList()

        buttons.add(
            InlineKeyboardButton.CallbackData(
                text = "+4",
                callbackData = "${callback}_max"
            )
        )

        return InlineKeyboardMarkup.Companion.create(listOf(buttons))
    }

    fun getAdTypeKeyboard(lang: String): InlineKeyboardMarkup {
        val buttons = listOf("all", "private").map { type ->
            InlineKeyboardButton.CallbackData(
                text = resources.getTranslation(lang, "ad_type_$type"),
                callbackData = "${Callbacks.CALLBACK_AD_TYPE}_$type"
            )
        }
        return InlineKeyboardMarkup.create(buttons.chunked(1))
    }

    fun getConfirmationKeyboard(lang: String): InlineKeyboardMarkup {
        val keyboards = listOf(
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = resources.getTranslation(lang, "keyboard_btn_correct"),
                    callbackData = "${Callbacks.CALLBACK_CONFIRMATION}_correct"
                ),
                InlineKeyboardButton.CallbackData(
                    text = resources.getTranslation(lang, "keyboard_btn_restart"),
                    callbackData = "${Callbacks.CALLBACK_CONFIRMATION}_restart"
                ),
            )
        )
        return InlineKeyboardMarkup.create(keyboards)
    }

//
//    fun getMainKeyboard(config: UserConfig): InlineKeyboardMarkup {
//        val secondAction = if (config.enabled && config.finished) "settings_bot_stop" else "settings_bot_start"
//
//        val keyboardButtons = listOf(
//            listOf(
//                InlineKeyboardButton(
//                    translates[config.lang]["settings_change_language"],
//                    callback_data = Callbacks.CALLBACK_MAIN + "change_language"
//                )
//            ),
//            listOf(
//                InlineKeyboardButton(
//                    translates[config.lang][secondAction],
//                    callback_data = Callbacks.CALLBACK_MAIN + secondAction
//                )
//            )
//        )
//
//        return InlineKeyboardMarkup().setKeyboard(keyboardButtons)
//    }
//
//    fun getSettingsKeyboard(config: UserConfig): ReplyKeyboardMarkup {
//        val keyboardButton = KeyboardButton(translates[config.lang]["bot_settings"])
//        val keyboardRow = listOf(keyboardButton)
//
//        return ReplyKeyboardMarkup().setKeyboard(listOf(keyboardRow)).setResizeKeyboard(true)
//    }
//
//    fun getAdTypeKeyboard(config: UserConfig): InlineKeyboardMarkup {
//        val buttons = localization["ad_types"].map { type ->
//            InlineKeyboardButton(
//                translates[config.lang][Callbacks.CALLBACK_AD_TYPE + type],
//                callback_data = Callbacks.CALLBACK_AD_TYPE + type
//            )
//        }
//
//        val chunkedButtons = buttons.chunked(1)
//
//        return InlineKeyboardMarkup().setKeyboard(chunkedButtons)
//    }
//
//
//    fun getRoomKeyboard(isMin: Boolean): InlineKeyboardMarkup {
//        val buttons = (1 until 4).map { number ->
//            InlineKeyboardButton(
//                number.toString(),
//                callback_data = (if (isMin) Callbacks.CALLBACK_ROOM_MIN else Callbacks.CALLBACK_ROOM_MAX) + number.toString()
//            )
//        }.toMutableList()
//
//        buttons.add(
//            InlineKeyboardButton(
//                "4+",
//                callback_data = (if (isMin) Callbacks.CALLBACK_ROOM_MIN else Callbacks.CALLBACK_ROOM_MAX) + "max"
//            )
//        )
//
//        val chunkedButtons = buttons.chunked(5)
//
//        return InlineKeyboardMarkup().setKeyboard(chunkedButtons)
//    }
//

//
//    fun reviewKeyboard(config: UserConfig): ReplyKeyboardMarkup {
//        val keyboardButtons = listOf(
//            listOf(translates[config.lang]["everything_is_great"]),
//            listOf(translates[config.lang]["i_have_error"], translates[config.lang]["i_have_suggestion"])
//        )
//
//        return ReplyKeyboardMarkup().setKeyboard(keyboardButtons).setResizeKeyboard(true)
//    }

}