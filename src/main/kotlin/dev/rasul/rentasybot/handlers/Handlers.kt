package dev.rasul.rentasybot.handlers

class Handlers(
    val startHandler: StartHandler,
    val languageHandler: LanguageHandler,
    val cityHandler: CityHandler,
    val districtsHandler: DistrictsHandler,
    val roomMinHandler: RoomMinHandler,
    val roomMaxHandler: RoomMaxHandler,
    val priceAndAreaHandler: PriceAndAreaHandler,
    val adTypeHandler: AdTypeHandler,
    val confirmationHandler: ConfirmationHandler,
    val unknownHandler: UnknownHandler
)