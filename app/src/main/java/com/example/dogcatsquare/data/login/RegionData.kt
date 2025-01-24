package com.example.dogcatsquare.data.login

object RegionData {
    val regions = listOf(
        Region(
            name = "서울",
            subRegions = listOf(
                SubRegion(name = "서울 전체", districts = listOf("")),
                SubRegion(name = "강남구", districts = listOf("강남구 전체", "고일동", "무슨동")),
                SubRegion(name = "강동구", districts = listOf("강동구 전체", "길동", "둔촌동")),
                SubRegion(name = "종로구", districts = listOf("종로구 전체", "창신동", "숭인동"))
            )
        ),
        Region(
            name = "경기",
            subRegions = listOf(
                SubRegion(name = "경기 전체", districts = listOf("")),
                SubRegion(name = "수원시", districts = listOf("수원시 전체", "영통구", "장안구")),
                SubRegion(name = "성남시", districts = listOf("성남시 전체", "분당구", "중원구")),
                SubRegion(name = "안양시", districts = listOf("안양시 전체"))
            )
        ),
        Region(
            name = "인천",
            subRegions = listOf(
                SubRegion(name = "인천 전체", districts = listOf("")),
                SubRegion(name = "부평구", districts = listOf("부평구 전체", "산곡동", "부개동")),
                SubRegion(name = "남동구", districts = listOf("남동구 전체", "구월동", "논현동")),
                SubRegion(name = "미추홀구", districts = listOf("미추홀구 전체", "용현동", "숭의동"))
            )
        ),
        Region(
            name = "강원",
            subRegions = listOf(
                SubRegion(name = "강원 전체", districts = listOf("")),
                SubRegion(name = "춘천시", districts = listOf("춘천시 전체", "효자동", "석사동")),
                SubRegion(name = "원주시", districts = listOf("원주시 전체", "무실동", "혁신도시")),
                SubRegion(name = "강릉시", districts = listOf("강릉시 전체", "교동", "포남동"))
            )
        ),
        Region(
            name = "대전",
            subRegions = listOf(
                SubRegion(name = "대전 전체", districts = listOf("")),
                SubRegion(name = "동구", districts = listOf("동구 전체", "대동", "자양동")),
                SubRegion(name = "서구", districts = listOf("서구 전체", "둔산동", "갈마동")),
                SubRegion(name = "유성구", districts = listOf("유성구 전체", "봉명동", "구암동"))
            )
        )
    )
}