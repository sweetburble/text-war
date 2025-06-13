package com.bandi.textwar.domain.repository

import arrow.core.Either
import java.io.InputStream

/**
 * 파일 스토리지를 위한 Repository 인터페이스
 */
interface StorageRepository {
    /**
     * 이미지를 스토리지에 업로드하고 공개 URL을 반환
     *
     * @param fileName 저장될 파일의 이름 (확장자 포함, 예: "battle_image_123.png")
     * @param inputStream 업로드할 이미지 데이터의 InputStream
     * @param mimeType 이미지의 MIME 타입 (예: "image/png")
     * @return 업로드 성공 시 공개 URL(String), 실패 시 오류(Throwable)를 담은 Either
     */
    suspend fun uploadImage(
        fileName: String,
        inputStream: InputStream,
        mimeType: String
    ): Either<Throwable, String>

    /**
     * 이미지를 스토리지에 업로드하고 공개 URL을 반환 (ByteArray 버전)
     *
     * @param fileNamePath 저장될 파일의 이름 (확장자 포함, 예: "battle_image_123.png")
     * @param byteArray 업로드할 이미지 데이터 (Byte Array)
     * @param mimeType 이미지의 MIME 타입 (예: "image/png")
     * @return 업로드 성공 시 공개 URL(String), 실패 시 오류(Throwable)를 담은 Either
     */
    suspend fun uploadImage(
        fileNamePath: String,
        byteArray: ByteArray,
        mimeType: String
    ): Either<Throwable, String>
} 