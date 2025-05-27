package com.bandi.textwar

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bandi.textwar.data.remote.SupabaseInstance
import com.bandi.textwar.ui.theme.TextWarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Supabase 클라이언트 초기화 확인 로그
        try {
            val client = SupabaseInstance.client
            Log.d("SupabaseInit", "Supabase client initialized successfully: $client")
            // 간단한 테스트 호출 (선택 사항, Supabase 프로젝트 설정에 따라 실패할 수 있음)
            // lifecycleScope.launch {
            //     try {
            //         // 예시: 가장 기본적인 users 테이블의 존재 여부 확인 (인증 불필요, 테이블 접근 권한 필요)
            //         // 실제로는 더 의미 있는 테스트 호출을 해야 합니다.
            //         // 이 호출은 RLS 정책 등에 따라 실패할 수 있으므로 주의해야 합니다.
            //         // val result = client.postgrest.from("users").select().count(Count.EXACT)
            //         // Log.d("SupabaseInit", "Supabase test call successful: ${result.count}")
            //     } catch (e: Exception) {
            //         Log.e("SupabaseInit", "Supabase test call failed", e)
            //     }
            // }
        } catch (e: Exception) {
            Log.e("SupabaseInit", "Supabase client initialization failed", e)
        }

        setContent {
            TextWarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TextWarTheme {
        Greeting("Android")
    }
}