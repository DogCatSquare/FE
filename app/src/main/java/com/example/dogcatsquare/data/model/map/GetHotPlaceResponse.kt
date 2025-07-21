package com.example.dogcatsquare.data.model.map

import com.google.gson.annotations.SerializedName

data class GetHotPlaceResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: List<GetHotPlaceResult>
)
data class GetHotPlaceResult(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("address") var address: String,
    @SerializedName("category") var category: String,
    @SerializedName("phoneNumber") var phoneNumber: String,
    @SerializedName("longitude") var longitude: Double,
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("distance") var distance: Double,
    @SerializedName("open") var open: Boolean,
    @SerializedName("imgUrl") var imgUrl: String?,
    @SerializedName("reviewCount") var reviewCount: Int
)