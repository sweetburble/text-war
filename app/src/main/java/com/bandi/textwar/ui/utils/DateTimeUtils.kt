package com.bandi.textwar.ui.utils

import timber.log.Timber
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * UTC 문자열을 KST ZonedDateTime으로 변환하는 함수
 */
fun String.toKstZonedDateTime(): ZonedDateTime? {
    return try {
        val offsetDateTime = OffsetDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        offsetDateTime.atZoneSameInstant(ZoneId.of("Asia/Seoul"))
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse or convert to KST: $this")
        null
    }
}

/**
 * ISO 8601 형식의 날짜/시간 문자열을 "yy-MM-dd HH:mm" 형식으로 변환합니다.
 * 파싱에 실패하면 원본 문자열을 반환합니다.
 */
fun String.toFormattedBattleTime(): String {
    return try {
        val zonedDateTimeKst = this.toKstZonedDateTime()
        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm")
        zonedDateTimeKst?.format(formatter) ?: this // 변환 실패 시 원본 반환
    } catch (e: Exception) {
        Timber.e(e, "Failed to format KST date-time string: $this")
        this
    }
}