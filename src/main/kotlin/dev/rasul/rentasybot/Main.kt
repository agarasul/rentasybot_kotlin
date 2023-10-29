package dev.rasul.rentasybot

import dev.rasul.rentasybot.config.BuildConfig
import dev.rasul.rentasybot.di.Modules
import org.koin.core.context.GlobalContext
import org.koin.mp.KoinPlatformTools


class Main {
    companion object {
        val app by lazy {
            KoinPlatformTools.defaultContext().get().get<App>()
        }

        @JvmStatic
        fun main(args: Array<String>) {
            BuildConfig.isDebug = true
            GlobalContext.startKoin {
                modules(Modules.appModule, Modules.handlers, Modules.parsers)
            }

            app.initApp()
        }
    }
}