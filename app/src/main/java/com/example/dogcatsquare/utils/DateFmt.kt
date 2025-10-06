package com.example.dogcatsquare.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

object DateFmt {
    private val OUT_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    // ISO-8601(UTC Z 포함/미포함), 마이크로/나노초, epochMillis(숫자 문자열)까지 전부 처리
    fun format(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return try {
            when {
                raw.last() == 'Z' || raw.last() == 'z' -> {
                    // e.g. 2025-10-06T16:22:43.918Z  (UTC)
                    val zdt = Instant.parse(raw).atZone(KST)
                    OUT_FMT.format(zdt)
                }
                raw.all { it.isDigit() } -> {
                    // e.g. "1728142345123" (epoch millis)
                    val epochMillis = raw.toLong()
                    val zdt = Instant.ofEpochMilli(epochMillis).atZone(KST)
                    OUT_FMT.format(zdt)
                }
                else -> {
                    // e.g. 2025-10-05T08:44:35.169942 (no zone)
                    val inFmt = DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                        .optionalStart()
                        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                        .optionalEnd()
                        .toFormatter()

                    val ldt = LocalDateTime.parse(raw, inFmt)
                    OUT_FMT.format(ldt.atZone(KST))
                }
            }
        } catch (_: Exception) {
            raw
        }
    }
}