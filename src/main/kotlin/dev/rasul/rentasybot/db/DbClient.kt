package dev.rasul.rentasybot.db

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.rasul.rentasybot.config.AppConfig
import dev.rasul.rentasybot.models.UserInfo

class DbClient(
    private val config : AppConfig
) {
    private val database by lazy {
        client.getDatabase(config.getDbName())
    }

    val usersCollection
        get() = database.getCollection<UserInfo>(config.getCollectionName())


    private val client: MongoClient by lazy {
        val connectionString =
            "mongodb+srv://rentasy_user:u4N4oGq9XEnTFXNS@rentasy-cluster.uxxnry0.mongodb.net/?retryWrites=true&w=majority"
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()

        val clientConfig = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .serverApi(serverApi)
            .build()
        MongoClient.create(clientConfig)
    }
}