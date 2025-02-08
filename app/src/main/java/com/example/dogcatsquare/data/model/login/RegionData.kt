package com.example.dogcatsquare.data.model.login

object RegionData {
    val regions = listOf(
        Region(
            name = "서울",
            subRegions = listOf(
                SubRegion(name = "강남구", districts = listOf("개포동", "논현동", "대치동", "도곡동", "삼성동", "세곡동", "수서동", "신사동", "압구정동", "역삼동", "율현동", "일원동", "청담동")),
                SubRegion(name = "강동구", districts = listOf("강일동", "고덕동", "길동", "둔촌동", "명일동", "상일동", "성내동", "암사동", "천호동")),
                SubRegion(name = "강북구", districts = listOf("미아동", "번동", "수유동", "우이동")),
                SubRegion(name = "강서구", districts = listOf("가양동", "개화동", "공항동", "내발산동", "등촌동", "마곡동", "방화동", "염창동", "오곡동", "외발산동", "화곡동")),
                SubRegion(name = "관악구", districts = listOf("남현동", "봉천동", "신림동")),
                SubRegion(name = "광진구", districts = listOf("광장동", "구의동", "군자동", "능동", "자양동", "중곡동", "화양동")),
                SubRegion(name = "구로구", districts = listOf("가리봉동", "개봉동", "고척동", "구로동", "신도림동", "오류동", "온수동", "천왕동", "항동")),
                SubRegion(name = "금천구", districts = listOf("가산동", "독산동", "시흥동")),
                SubRegion(name = "노원구", districts = listOf("공릉동", "상계동", "월계동", "중계동", "하계동")),
                SubRegion(name = "도봉구", districts = listOf("도봉동", "방학동", "쌍문동", "창동")),
                SubRegion(name = "동대문구", districts = listOf("답십리동", "신설동", "용두동", "이문동", "장안동", "전농동", "제기동", "청량리동", "회기동", "휘경동")),
                SubRegion(name = "동작구", districts = listOf("노량진동", "대방동", "동작동", "본동", "사당동", "상도동", "신대방동", "흑석동")),
                SubRegion(name = "마포구", districts = listOf("공덕동", "노고산동", "대흥동", "도화동", "동교동", "마포동", "망원동", "상수동", "상암동", "성산동", "신공덕동", "신수동", "신정동", "아현동", "연남동", "염리동", "용강동", "중동", "창전동", "토정동", "하중동", "합정동", "현석동")),
                SubRegion(name = "서대문구", districts = listOf("남가좌동", "북가좌동", "북아현동", "신촌동", "연희동", "영천동", "옥천동", "창천동", "천연동", "충정로", "홍제동", "홍은동")),
                SubRegion(name = "서초구", districts = listOf("내곡동", "반포동", "방배동", "서초동", "신원동", "양재동", "우면동", "잠원동")),
                SubRegion(name = "성동구", districts = listOf("금호동", "도선동", "마장동", "사근동", "성수동", "송정동", "옥수동", "왕십리동", "용답동", "응봉동", "하왕십리동", "행당동", "홍익동")),
                SubRegion(name = "성북구", districts = listOf("길음동", "돈암동", "동선동", "보문동", "삼선동", "석관동", "성북동", "안암동", "월곡동", "장위동", "정릉동", "종암동")),
                SubRegion(name = "송파구", districts = listOf("가락동", "거여동", "마천동", "문정동", "방이동", "삼전동", "석촌동", "송파동", "신천동", "오금동", "잠실동", "장지동", "풍납동")),
                SubRegion(name = "양천구", districts = listOf("목동", "신월동", "신정동")),
                SubRegion(name = "영등포구", districts = listOf("당산동", "대림동", "도림동", "문래동", "신길동", "양평동", "영등포동", "여의도동")),
                SubRegion(name = "용산구", districts = listOf("갈월동", "남영동", "보광동", "서빙고동", "용산동", "이촌동", "이태원동", "한남동", "효창동")),
                SubRegion(name = "은평구", districts = listOf("갈현동", "구산동", "녹번동", "대조동", "불광동", "수색동", "신사동", "역촌동", "응암동", "증산동", "진관동")),
                SubRegion(name = "종로구", districts = listOf("가회동", "계동", "누하동", "창신동", "평창동", "혜화동")),
                SubRegion(name = "중구", districts = listOf("남대문로", "다산동", "명동", "소공동", "을지로", "황학동")),
                SubRegion(name = "중랑구", districts = listOf("망우동", "면목동", "묵동", "상봉동", "신내동"))
            )
        ),
        Region(
            name = "경기",
            subRegions = listOf(
                SubRegion(name = "경기 전체", districts = listOf("")),
                SubRegion(name = "가평군", districts = listOf("가평읍", "설악면", "청평면", "상면", "조종면", "하면")),
                SubRegion(name = "고양시", districts = listOf("덕양구", "일산동구", "일산서구")),
                SubRegion(name = "과천시", districts = listOf("과천동", "문원동", "별양동", "부림동", "갈현동")),
                SubRegion(name = "광명시", districts = listOf("광명동", "소하동", "철산동", "하안동")),
                SubRegion(name = "광주시", districts = listOf("경안동", "송정동", "탄벌동", "쌍령동", "회덕동")),
                SubRegion(name = "구리시", districts = listOf("갈매동", "교문동", "수택동", "인창동")),
                SubRegion(name = "군포시", districts = listOf("군포동", "대야동", "산본동", "수리동")),
                SubRegion(name = "김포시", districts = listOf("통진읍", "고촌읍", "대곶면", "양촌읍", "하성면", "장기동", "구래동")),
                SubRegion(name = "남양주시", districts = listOf("화도읍", "진접읍", "진건읍", "오남읍", "퇴계원읍", "별내동")),
                SubRegion(name = "동두천시", districts = listOf("생연동", "송내동", "지행동", "탑동")),
                SubRegion(name = "부천시", districts = listOf("소사구", "오정구", "원미구")),
                SubRegion(name = "성남시", districts = listOf("분당구", "중원구", "수정구")),
                SubRegion(name = "수원시", districts = listOf("영통구", "장안구", "팔달구", "권선구")),
                SubRegion(name = "시흥시", districts = listOf("신천동", "대야동", "장곡동", "정왕동")),
                SubRegion(name = "안산시", districts = listOf("상록구", "단원구")),
                SubRegion(name = "안성시", districts = listOf("공도읍", "미양면", "서운면", "대덕면")),
                SubRegion(name = "안양시", districts = listOf("동안구", "만안구")),
                SubRegion(name = "양주시", districts = listOf("광적면", "백석읍", "양주읍", "남면")),
                SubRegion(name = "양평군", districts = listOf("양평읍", "옥천면", "서종면", "단월면")),
                SubRegion(name = "여주시", districts = listOf("여흥동", "중앙동", "흥천면", "강천면")),
                SubRegion(name = "연천군", districts = listOf("전곡읍", "연천읍", "청산면", "신서면")),
                SubRegion(name = "오산시", districts = listOf("가장동", "갈곶동", "오산동", "수청동")),
                SubRegion(name = "용인시", districts = listOf("기흥구", "수지구", "처인구")),
                SubRegion(name = "의왕시", districts = listOf("고천동", "부곡동", "청계동")),
                SubRegion(name = "의정부시", districts = listOf("호원동", "신곡동", "용현동", "의정부동")),
                SubRegion(name = "이천시", districts = listOf("부발읍", "대월면", "백사면", "율면")),
                SubRegion(name = "파주시", districts = listOf("문산읍", "운정동", "조리읍", "탄현면")),
                SubRegion(name = "평택시", districts = listOf("팽성읍", "안중읍", "포승읍", "청북읍")),
                SubRegion(name = "포천시", districts = listOf("일동면", "이동면", "창수면", "내촌면")),
                SubRegion(name = "하남시", districts = listOf("망월동", "신장동", "덕풍동", "풍산동")),
                SubRegion(name = "화성시", districts = listOf("봉담읍", "우정읍", "향남읍", "동탄동"))
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