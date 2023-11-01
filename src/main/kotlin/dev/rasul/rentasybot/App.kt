package dev.rasul.rentasybot

import dev.rasul.rentasybot.bot.RentasyBot
import dev.rasul.rentasybot.parsers.ParsersManager

class App(
    private val rentasyBot: RentasyBot,
    private val parsersManager: ParsersManager
) {
    fun initApp() {
        rentasyBot.start()
        rentasyBot.sendAds()
        parsersManager.start()
    }
}