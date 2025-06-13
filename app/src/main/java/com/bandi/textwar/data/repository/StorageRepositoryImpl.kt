package com.bandi.textwar.data.repository

import arrow.core.Either
import com.bandi.textwar.data.datasource.StorageRemoteDataSource
import com.bandi.textwar.domain.repository.StorageRepository
import java.io.InputStream
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val remoteDataSource: StorageRemoteDataSource
) : StorageRepository {

    private val BUCKET_NAME = "battle-images"

    override suspend fun uploadImage(
        fileName: String,
        inputStream: InputStream,
        mimeType: String
    ): Either<Throwable, String> {
        // 파일 이름에 경로가 포함될 수 있으므로, 순수 파일 이름만 사용하거나, 필요시 경로를 추가
        // 여기서는 fileName을 그대로 path로 사용
        return remoteDataSource.uploadImage(BUCKET_NAME, fileName, inputStream, mimeType)
    }

    override suspend fun uploadImage(
        fileNamePath: String,
        byteArray: ByteArray,
        mimeType: String
    ): Either<Throwable, String> {
        return remoteDataSource.uploadImage(
            bucketName = BUCKET_NAME,
            fileNamePath = fileNamePath,
            byteArray = byteArray,
            mimeType = mimeType
        )
    }
} 