package com.example.dogcatsquare.data.model.pet

data class PetList(
    val id: Int,
    val petName: String,
    val dogCat: String,
    val breed: String,
    val birth: String,
    val petImageUrl: String?
)