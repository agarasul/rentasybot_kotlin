package dev.rasul.rentasybot.parsers

import dev.rasul.rentasybot.models.UserInfo

interface AdParser {


    suspend fun startParsing(config: UserInfo)
}