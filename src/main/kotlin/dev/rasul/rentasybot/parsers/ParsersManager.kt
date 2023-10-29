package dev.rasul.rentasybot.parsers

import dev.rasul.rentasybot.db.UserConfigDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ParsersManager(
    private val scope: CoroutineScope,
    private val userConfigDao: UserConfigDao,
    private val parsers: List<AdParser>
) {


    fun start() {
        scope.launch {
            userConfigDao.getUserConfigs(finished = true, enabled = true).collect { config ->
                parsers.forEach { parser ->
                    launch {
                        try {
                            parser.startParsing(config)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}