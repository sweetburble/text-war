package com.bandi.textwar.data.datasource

import arrow.core.Either
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject


class StorageRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun uploadImage(
        bucketName: String,
        path: String,
        inputStream: InputStream,
        mimeType: String
    ): Either<Throwable, String> {
        return try {
            Timber.d("Supabase Storage 이미지 업로드 시작: 버킷=$bucketName, 경로=$path, MIME=$mimeType")
            val byteArray = inputStream.readBytes()
            supabaseClient.storage.from(bucketName).upload(path = path, data = byteArray) {
                upsert = true
                contentType = ContentType.parse(mimeType)
            }
            val publicUrl = supabaseClient.storage.from(bucketName).publicUrl(path)
            Timber.i("Supabase Storage 이미지 업로드 성공: $publicUrl")
            Either.Right(publicUrl)
        } catch (e: Exception) {
            Timber.e(e, "Supabase Storage 이미지 업로드 실패: 버킷=$bucketName, 경로=$path")
            Either.Left(e)
        }
    }

    suspend fun uploadImage(
        bucketName: String,
        fileNamePath: String,
        byteArray: ByteArray,
        mimeType: String
    ): Either<Throwable, String> {
        return try {
            Timber.d("Supabase Storage 이미지 업로드 시작 (ByteArray): 버킷=$bucketName, 경로=$fileNamePath, MIME=$mimeType")
            supabaseClient.storage.from(bucketName).upload(path = fileNamePath, data = byteArray) {
                upsert = true
                contentType = ContentType.parse(mimeType)
            }
            val publicUrl = supabaseClient.storage.from(bucketName).publicUrl(fileNamePath)
            Timber.i("Supabase Storage 이미지 업로드 성공 (ByteArray): $publicUrl")
            Either.Right(publicUrl)
        } catch (e: Exception) {
            Timber.e(e, "Supabase Storage 이미지 업로드 실패 (ByteArray): 버킷=$bucketName, 경로=$fileNamePath")
            Either.Left(e)
        }
    }
} 