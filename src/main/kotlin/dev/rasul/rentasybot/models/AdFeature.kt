package dev.rasul.rentasybot.models

import org.bson.codecs.pojo.annotations.BsonProperty

data class AdFeature(
    @BsonProperty("title") val title: String? = null,
    @BsonProperty("price") val price: String? = null,
    @BsonProperty("deposit") val deposit: String? = null,
    @BsonProperty("administrative_rent") val administrativeRent: String? = null,
    @BsonProperty("room") val room: String? = null,
    @BsonProperty("floor") val floor: String? = null,
    @BsonProperty("area") val area: String? = null,
    @BsonProperty("url") val url: String? = null,
    @BsonProperty("created_at") val createdAt: String? = null,
    @BsonProperty("city") val city: String? = null,
    @BsonProperty("district") val district: String? = null,
    @BsonProperty("source") val source: String? = null
)