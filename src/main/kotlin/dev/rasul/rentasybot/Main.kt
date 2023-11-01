package dev.rasul.rentasybot

import dev.rasul.rentasybot.di.Modules
import org.koin.core.context.GlobalContext.startKoin
import org.koin.mp.KoinPlatformTools


class Main {
    companion object {
        private val app by lazy {
            KoinPlatformTools.defaultContext().get().get<App>()
        }

        @JvmStatic
        fun main(args: Array<String>) {
            startKoin {
                modules(Modules.appModule, Modules.handlers, Modules.parsers, Modules.queues)
            }

            app.initApp()
        }
    }
}