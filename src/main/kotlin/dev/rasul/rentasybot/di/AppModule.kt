package dev.rasul.rentasybot.di

import KeyboardHelper
import OlxParser
import OtodomParser
import com.google.gson.Gson
import dev.rasul.rentasybot.App
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.bot.RentasyBot
import dev.rasul.rentasybot.config.AppConfig
import dev.rasul.rentasybot.db.DbClient
import dev.rasul.rentasybot.db.UserConfigDAOImpl
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.handlers.*
import dev.rasul.rentasybot.helper.MessageCaptionFormatter
import dev.rasul.rentasybot.parsers.AdParser
import dev.rasul.rentasybot.parsers.ParsersManager
import dev.rasul.rentasybot.queue.AdsMessageQueue
import dev.rasul.rentasybot.queue.ErrorMessageQueue
import dev.rasul.rentasybot.queue.MessageQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


object Modules {


    val appModule = module {
        factory { CoroutineScope(Dispatchers.IO + Job()) }
        single {
            DbClient(get())
        }

        single {
            OkHttpClient.Builder().build()
        }
        single {
            Gson()
        }

        singleOf(::Resources)

        singleOf(::AppConfig)

        single<UserConfigDao> { UserConfigDAOImpl(get(), get()) }



        singleOf(::KeyboardHelper)

        singleOf(::MessageCaptionFormatter)
        singleOf(::RentasyBot)


        singleOf(::ParsersManager)

        singleOf(::App)
    }


    val queues = module {
        singleOf(::AdsMessageQueue)
        singleOf(::ErrorMessageQueue)
        singleOf(::MessageQueue)
    }

    val handlers = module {
        singleOf(::StartHandler)
        singleOf(::LanguageHandler)
        singleOf(::CityHandler)
        singleOf(::DistrictsHandler)
        singleOf(::RoomMinHandler)
        singleOf(::RoomMaxHandler)
        singleOf(::PriceAndAreaHandler)
        singleOf(::AdTypeHandler)
        singleOf(::ConfirmationHandler)
        singleOf(::UnknownHandler)
        singleOf(::Handlers)
    }

    val parsers = module {
        singleOf(::OlxParser)
        singleOf(::OtodomParser)
        single<Set<AdParser>> {
            setOf(
                get(OtodomParser::class),
                get(OlxParser::class)
            )
        }
    }
}