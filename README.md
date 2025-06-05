# Text War

## 🎮 애플리케이션 개요

Text War는 사용자가 텍스트 설명을 통해 자신만의 고유한 캐릭터를 생성하고, 다른 사용자의 캐릭터와 AI가 판정하는 배틀을 즐길 수 있는 Android 모바일 애플리케이션입니다. 이 게임은 창의적인 글쓰기를 좋아하는 사용자, AI 기술에 관심 있는 사용자, 그리고 간단하면서도 독특한 경쟁을 즐기고 싶은 사용자들을 위한 것입니다. Text War는 사용자의 상상력을 자극하고, AI를 통해 예측 불가능하고 흥미로운 배틀 경험을 제공하여 기존 모바일 게임과는 차별화된 가치를 제공합니다.

## ✨ 주요 기능

1. **✍️ 텍스트 기반 캐릭터 생성**
    - 사용자는 100자 이내의 텍스트로 자신만의 캐릭터의 외형, 능력, 배경 등을 자유롭게 설명하여 생성합니다.
2. **🤖 AI 기반 배틀 시스템 (AI 모델 활용)**
    - 두 캐릭터의 텍스트 설명을 입력받아, AI 모델이 배틀의 과정과 결과를 판정하고 서술합니다.
3. **🖼️ 배틀 결과 시각화**
    - AI가 생성한 배틀 결과(승/패), 배틀 상황 해설 텍스트, 그리고 이 해설을 기반으로 생성된 한 장의 이미지를 사용자에게 보여줍니다.
4. **💰 캐릭터 슬롯 및 배틀 쿨타임 관리 (유료화 모델)**
    - 기본 제공되는 캐릭터 슬롯 외에 추가 슬롯을 구매하거나, 배틀 후 발생하는 쿨타임을 유료 재화(토큰)를 사용하여 즉시 해제할 수 있습니다.
5. **🏆 리더보드 시스템**
    - 모든 사용자 캐릭터들의 승리, 패배, 랭킹 등을 집계하여 순위를 보여주는 화면을 제공합니다.
6. **🔒 사용자 인증 (로그인)**
    - 사용자가 계정을 생성하고 로그인하여 자신의 캐릭터, 구매 내역, 배틀 기록 등을 안전하게 관리할 수 있도록 합니다.

## 🛠️ 기술 스택

-   **Android Client App:**
    -   UI: Jetpack Compose
    -   Language: Kotlin
    -   Architecture: MVVM (Model-View-ViewModel)
    -   Asynchronous Programming: Coroutines
    -   Dependency Injection: Hilt
    -   API Communication: Retrofit (for OpenAI API)
-   **Backend:** Supabase
    -   Authentication: Supabase Auth (이메일/비밀번호, 소셜 로그인 등)
    -   Database: Supabase Database (PostgreSQL) - 사용자 데이터, 캐릭터 데이터, 배틀 기록, 리더보드 데이터 저장
    -   Serverless Functions: Supabase Edge Functions (Optional - 복잡한 로직 처리)
    -   Storage: Supabase Storage (Optional - AI 생성 이미지 캐싱/저장)
-   **AI Model:** 현재 OpenAI GPT API (텍스트 기반 배틀 판정, 해설 생성, 이미지 생성)
-   **Payment Gateway:** Google Play Billing Library

## 📱 화면 예시

![Image](https://github.com/user-attachments/assets/82762f02-781f-4ed3-aade-e2300aa6c4b6)

## 🚀 시작 가이드

### 사전 요구 사항

-   Android Studio (최신 버전 권장)
-   Android SDK (Min SDK 26, Target SDK 최신 버전)
-   Kotlin

### 빌드 및 실행 방법 2가지

1. release에서 APK 파일을 다운로드 받아 설치합니다.
   </br></br></br>

2-1. **프로젝트 클론:**

```bash
git clone https://github.com/your-username/text-war.git
cd text-war
```

2-2. **API 키 설정:**

-   프로젝트 루트 디렉토리에 `local.properties` 파일을 생성합니다.
-   아래 내용을 참고하여 Supabase 및 OpenAI API 키를 입력합니다.

```properties
# Supabase
SUPABASE_URL=YOUR_SUPABASE_URL_HERE
SUPABASE_ANON_KEY=YOUR_SUPABASE_ANON_KEY_HERE

# OpenAI (선택 사항, 직접 API 호출 시)
OPENAI_API_KEY=YOUR_OPENAI_API_KEY_HERE
```

-   `YOUR_..._HERE` 부분을 실제 발급받은 키로 대체합니다.
    </br></br>

2-3. **Android Studio에서 프로젝트 열기:** - Android Studio를 실행하고 'Open an existing Android Studio project'를 선택하여 클론한 `text-war` 폴더를 엽니다.

2-4. **Gradle 동기화:** - 프로젝트가 열리면 Android Studio가 자동으로 Gradle 동기화를 진행합니다. 완료될 때까지 기다립니다.

2-5. **애플리케이션 실행:** - 연결된 Android 기기 또는 에뮬레이터를 선택하고 'Run' 버튼 (▶️)을 클릭하여 애플리케이션을 빌드하고 실행합니다.
