package dev.rasul.rentasybot.models

import org.bson.codecs.pojo.annotations.BsonProperty

data class UserCriteria(
    @BsonProperty("city") val city: Int? = null,
    @BsonProperty("all_districts") val allDistricts: Boolean = true,
    @BsonProperty("district") val district: List<Int> = emptyList(),
    @BsonProperty("room_from") val roomFrom: Int? = null,
    @BsonProperty("room_to") val roomTo: Int? = null,
    @BsonProperty("price_from") val priceFrom: Int? = null,
    @BsonProperty("price_to") val priceTo: Int? = null,
    @BsonProperty("area_from") val areaFrom: Int? = null,
    @BsonProperty("area_to") val areaTo: Int? = null,
    @BsonProperty("only_from_owners") val onlyFromOwners: Boolean = false
)