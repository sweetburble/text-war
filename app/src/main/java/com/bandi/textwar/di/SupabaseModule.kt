package com.bandi.textwar.di

import com.bandi.textwar.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL, // BuildConfig에서 URL 가져오기
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY // BuildConfig에서 Key 가져오기
        ) {
            install(Auth) // Auth 플러그인 설치
            install(Postgrest) // Postgrest 플러그인 설치 (데이터베이스 사용 시 필요)
            install(Storage) // 저장소 기능
            // 필요한 다른 Supabase 플러그인이 있다면 여기에 추가
        }
    }

    // Supabase Auth 인스턴스 DI 제공
    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }
}