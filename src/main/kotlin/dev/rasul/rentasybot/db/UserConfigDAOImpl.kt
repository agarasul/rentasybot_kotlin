package dev.rasul.rentasybot.db

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.ReturnDocument
import dev.rasul.rentasybot.models.UserConfig
import kotlinx.coroutines.flow.Flow

class UserConfigDAOImpl(
    private val dbClient: DbClient,
    private val gson: Gson
) : UserConfigDao {


    override suspend fun getUserConfigs(finished: Boolean, enabled: Boolean): Flow<UserConfig> {
        return dbClient.usersCollection.find<UserConfig>(
            Filters.and(
                Filters.eq(UserConfig::finished.name, true),
                Filters.eq(UserConfig::enabled.name, true)
            )
        )
    }

    override suspend fun deleteUserConfig(userConfig: UserConfig): Boolean {
        return dbClient.usersCollection.deleteOne(
            Filters.eq("_id", userConfig.id)
        ).wasAcknowledged()
    }

    override suspend fun updateUserConfig(userConfig: UserConfig): UserConfig? {


        return dbClient.usersCollection.findOneAndReplace(
            Filters.eq("_id", userConfig.id),
            userConfig,
            FindOneAndReplaceOptions().apply {
                upsert(true)
                returnDocument(ReturnDocument.AFTER)
            }
        )
    }
}