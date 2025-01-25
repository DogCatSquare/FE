package com.example.dogcatsquare.data.login

data class SignUpRequest(
    var email: String,
    var password: String,
    var nickname: String,
    var phoneNumber: String,
    var doName: String,
    var gu: String,
    var si: String,
    var pets: List<Pet>,
    var foodDate: String,
    var foodDuring: Int,
    var padDate: String,
    var padDuring: Int,
    var hospitalDate: String,
    var adAgree: Boolean
)
data class Pet(
    var petName: String,
    var dogCat: Enum<DogCat>,
    var breed: String,
    var birth: String
)
enum class DogCat {
    DOG,
    CAT
}