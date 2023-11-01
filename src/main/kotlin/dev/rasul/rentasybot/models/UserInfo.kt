package dev.rasul.rentasybot.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class UserInfo(
    @BsonId val id: ObjectId? = ObjectId(),
    @BsonProperty("telegramUserId") val telegramUserId: Long,
    @BsonProperty("username") val username: String? = null,
    @BsonProperty("firstname") val firstname: String? = null,
    @BsonProperty("lastname") val lastname: String? = null,
    @BsonProperty("lang") val lang: String = "en",
    @BsonProperty("criteria") val criteria: UserCriteria?,
    @BsonProperty("seenOtodomIds") val seenOtodomIds: List<String> = emptyList(),
    @BsonProperty("seenOlxIds") val seenOlxIds: List<String>? = emptyList(),
) {
    companion object {
        fun new(telegramUserId: Long, firstname: String?, lastname: String?): UserInfo {
            return UserInfo(
                telegramUserId = telegramUserId,
                firstname = firstname,
                lastname = lastname,
                criteria = UserCriteria()
            )
        }
    }
}