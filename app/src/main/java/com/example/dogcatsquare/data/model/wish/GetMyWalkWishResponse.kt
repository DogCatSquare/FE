package com.example.dogcatsquare.data.model.wish

import com.google.gson.annotations.SerializedName

data class GetMyWalkWishResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: WalkResult
)
data class WalkResult(
    @SerializedName("walks") val walks: List<Walk>
)

data class Walk(
    @SerializedName("walkId") val walkId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("walkImageUrl") val walkImageUrl: List<String>,
    @SerializedName("reviewCount") val reviewCount: Int,
    @SerializedName("distance") val distance: Double,
    @SerializedName("time") val time: Int,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("special") val special: List<Special>,
    @SerializedName("coordinates") val coordinates: List<Coordinate>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("createdBy") val createdBy: CreatedBy
)

data class Special(
    @SerializedName("type") val type: String,
    @SerializedName("customValue") val customValue: String
)

data class Coordinate(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("sequence") val sequence: Int
)

data class CreatedBy(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("breed") val breed: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String
)