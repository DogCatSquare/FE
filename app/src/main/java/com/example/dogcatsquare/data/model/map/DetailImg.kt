package com.example.dogcatsquare.data.model.map

data class DetailImg(
    val detailImg: Int? = null,
    val imageUrl: String? = null
) {
    constructor(resourceId: Int) : this(detailImg = resourceId, imageUrl = null)
    constructor(url: String) : this(detailImg = null, imageUrl = url)

    // URL이 유효한지 확인하는 헬퍼 함수
    fun isUrlImage(): Boolean = !imageUrl.isNullOrBlank()

    // 기본 이미지 리소스를 가져오는 헬퍼 함수
    fun getDefaultResource(): Int = com.example.dogcatsquare.R.drawable.ic_place_img_default
}