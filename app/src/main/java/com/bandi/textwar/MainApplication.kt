package com.bandi.textwar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    // Hilt 설정을 위해 Application 클래스에 @HiltAndroidApp 어노테이션 추가
    // 특별한 초기화 코드가 필요 없다면 내부는 비워둘 수 있음
    // SupabaseClient 초기화는 SupabaseModule에서 처리하므로 여기서는 별도 호출 필요 없음
}