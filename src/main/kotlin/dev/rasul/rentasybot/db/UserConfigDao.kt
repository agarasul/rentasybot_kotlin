package dev.rasul.rentasybot.db

import dev.rasul.rentasybot.models.UserInfo
import kotlinx.coroutines.flow.Flow
import org.bson.BsonObjectId

interface UserConfigDao {

    suspend fun insertUser(userInfoConfig: UserInfo): BsonObjectId?
    suspend fun updateUser(telegramChatId: Long, params: Map<String, Any>): UserInfo?


    suspend fun getUser(telegramUserId: Long): UserInfo?

    suspend fun getUsers(): Flow<UserInfo>

    suspend fun deleteUser(userInfoConfig: UserInfo): Boolean

}