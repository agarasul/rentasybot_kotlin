package dev.rasul.rentasybot.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class UserConfig(
    @BsonId val id: ObjectId,
    @BsonProperty("telegramUserId") val telegramUserId: Long,
    @BsonProperty("username") val username: String? = null,
    @BsonProperty("firstname") val firstname: String? = null,
    @BsonProperty("lastname") val lastname: String? = null,
    @BsonProperty("lang") val lang: String = "en",
    @BsonProperty("enabled") val enabled: Boolean? = null,
    @BsonProperty("criteria") val criteria: UserCriteria,
    @BsonProperty("seenOtodomIds") val seenOtodomIds: List<String> = emptyList(),
    @BsonProperty("seenOlxIds") val seenOlxIds: List<String>? = emptyList(),
    @BsonProperty("finished") val finished: Boolean? = null,
    @BsonProperty("subscribed") val subscribed: Boolean? = null,
    @BsonProperty("step") val step: Int? = null
)