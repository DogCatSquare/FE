package com.example.dogcatsquare.data.region

data class Region(
    val name: String, // 지역 이름 (서울, 경기 등)
    val subRegions: List<SubRegion> // 하위 지역 목록
)

data class SubRegion(
    val name: String, // 하위 지역 이름 (강남구, 강동구 등)
    val districts: List<String> // 세 번째 컬럼 데이터 (동 목록)
)
