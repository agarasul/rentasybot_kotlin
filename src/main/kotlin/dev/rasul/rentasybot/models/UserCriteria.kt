package dev.rasul.rentasybot.models

import org.bson.codecs.pojo.annotations.BsonProperty

data class UserCriteria(
    @BsonProperty("city") val city: Int? = null,
    @BsonProperty("allDistricts") val allDistricts: Boolean = false,
    @BsonProperty("district") val district: List<Int> = emptyList(),
    @BsonProperty("roomMin") val roomMin: Int? = null,
    @BsonProperty("roomMax") val roomMax: Int? = null,
    @BsonProperty("priceMin") val priceMin: Int? = null,
    @BsonProperty("priceMax") val priceMax: Int? = null,
    @BsonProperty("areaMin") val areaMin: Int? = null,
    @BsonProperty("areaMax") val areaMax: Int? = null,
    @BsonProperty("onlyFromOwners") val onlyFromOwners: Boolean = false,
    @BsonProperty("finished") val finished: Boolean = false,
    @BsonProperty("enabled") val enabled: Boolean = false,
    @BsonProperty("step") val step: String = Step.Start.name

){
    enum class Step {
        Start,
        Language,
        City,
        District,
        RoomMin,
        RoomMax,
        Price,
        Area,
        AdType,
        Confirmation,
        Finished
    }

}