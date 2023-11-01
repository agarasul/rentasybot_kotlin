package dev.rasul.rentasybot.parsers

import dev.rasul.rentasybot.db.UserConfigDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ParsersManager(
    private val scope: CoroutineScope,
    private val userConfigDao: UserConfigDao,
    private val parsers: Set<AdParser>
) {
    fun start() {
        scope.launch {
            while (true) {
                userConfigDao.getUsers().collect { config ->
                    parsers.forEach { parser ->
                        println("Parser Started = ${parser::class.java.name}")
                        launch {
                            try {
                                parser.startParsing(config)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                delay(TimeUnit.MINUTES.toMillis(30))
            }
        }
    }
}