package dev.rasul.rentasybot.db

import dev.rasul.rentasybot.models.UserConfig
import kotlinx.coroutines.flow.Flow

interface UserConfigDao {

    suspend fun updateUserConfig(userConfig: UserConfig): UserConfig?

    suspend fun getUserConfigs(finished: Boolean, enabled: Boolean): Flow<UserConfig>

    suspend fun deleteUserConfig(userConfig: UserConfig): Boolean
}