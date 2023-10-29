package dev.rasul.rentasybot.config

object BuildConfig {

    var isDebug = false

    const val DEVELOPER_CHAT_ID: Long = 302302391

    private const val BOT_API_KEY_PROD = "6671913226:AAGyPP1LRnSo9xvVwdVqgdUP_LpPx31SgDY"

    private const val BOT_API_KEY_DEV = "6635768392:AAH0Pqco21Bct3BYHPS1ho15TaKdumHUOBU"


    const val DB_NAME = "rentasy_db"

    const val USERS_COLLECTION_NAME = "users"

    const val USERS_MIGRATION_COLLECTION = "users_migration"

    fun getBotApiKey(): String {
        return if (isDebug) {
            BOT_API_KEY_DEV
        } else {
            BOT_API_KEY_PROD
        }
    }

    fun getCollectionName(): String {
        return if (isDebug) {
            USERS_MIGRATION_COLLECTION
        } else {
            USERS_COLLECTION_NAME
        }
    }


}