package dev.rasul.rentasybot.db

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import dev.rasul.rentasybot.models.UserCriteria
import dev.rasul.rentasybot.models.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.bson.BsonObjectId

class UserConfigDAOImpl(
    private val dbClient: DbClient,
    private val gson: Gson
) : UserConfigDao {


    override suspend fun getUser(telegramUserId: Long): UserInfo? {
        return dbClient.usersCollection.find<UserInfo>(
            Filters.eq(UserInfo::telegramUserId.name, telegramUserId)
        ).firstOrNull()
    }

    override suspend fun getUsers(): Flow<UserInfo> {
        return dbClient.usersCollection.find<UserInfo>(
            Filters.and(
                Filters.eq("criteria.${UserCriteria::finished.name}", true),
                Filters.eq("criteria.${UserCriteria::enabled.name}", true)
            )
        )
    }

    override suspend fun deleteUser(userInfoConfig: UserInfo): Boolean {
        return dbClient.usersCollection.deleteOne(
            Filters.eq("_id", userInfoConfig.id)
        ).wasAcknowledged()
    }

    override suspend fun insertUser(userInfoConfig: UserInfo): BsonObjectId {
        return dbClient.usersCollection.insertOne(userInfoConfig).insertedId?.asObjectId() ?: BsonObjectId()
    }


    override suspend fun updateUser(telegramChatId: Long, params: Map<String, Any>): UserInfo? {
        return dbClient.usersCollection.findOneAndUpdate(
            Filters.eq(UserInfo::telegramUserId.name, telegramChatId),
            Updates.combine(
                params.map { (key, value) ->
                    Updates.set(key, value)
                }
            ),
            FindOneAndUpdateOptions().apply {
                returnDocument(ReturnDocument.AFTER)
            }
        )
    }
}