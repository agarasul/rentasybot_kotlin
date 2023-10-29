package dev.rasul.rentasybot.parsers

import dev.rasul.rentasybot.models.UserConfig

interface AdParser {


    suspend fun startParsing(config: UserConfig)
}