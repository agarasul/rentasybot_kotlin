package dev.rasul.rentasybot.di

import OlxParser
import com.google.gson.Gson
import dev.rasul.rentasybot.App
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.bot.RentasyBot
import dev.rasul.rentasybot.db.DbClient
import dev.rasul.rentasybot.db.UserConfigDAOImpl
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.handlers.StartHandler
import dev.rasul.rentasybot.parsers.AdParser
import dev.rasul.rentasybot.parsers.MessageCaptionFormatter
import dev.rasul.rentasybot.parsers.ParsersManager
import dev.rasul.rentasybot.queue.AdsMessageQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import org.koin.dsl.module


object Modules {


    val appModule = module {
        single {
            DbClient()
        }

        single {
            OkHttpClient.Builder().build()
        }
        single {
            Gson()
        }
        single<UserConfigDao> { UserConfigDAOImpl(get(), get()) }
        factory { CoroutineScope(Dispatchers.IO + Job()) }
        single { Resources(get()) }

        single { AdsMessageQueue(get()) }

        single { MessageCaptionFormatter(get()) }

        single { RentasyBot(get(), get(), get(), get()) }


        single { ParsersManager(get(), get(), getAll<AdParser>()) }


        single { App(get(), get()) }

    }


    val handlers = module {
        single { StartHandler(get()) }
    }

    val parsers = module {
        single<AdParser> {
//            OtodomParser(get(), get(), get(), get(), get(), get())
            OlxParser(get(), get(), get(), get(), get(), get())
        }
    }
}