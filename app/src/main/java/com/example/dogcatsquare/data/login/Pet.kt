package com.example.dogcatsquare.data.login

data class Pet(
    val petName: String,
    val dogCat: Enum<DogCat>,
    val breed: String,
    val birth: String
)
enum class DogCat {
    DOG,
    CAT
}
