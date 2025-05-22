# 📡 SpaceUwb Android SDK

**SpaceUwb**는 FREEGROW Inc.의 UWB 기반 Android SDK로, BLE 장치 검색, 거리 측정, 방향 계산, RTLS 실시간 위치 추정 기능을 제공합니다.  
SDK 사용자는 단일 클래스 `SpaceUwb`를 통해 복잡한 연결 흐름 없이 UWB 기능을 간편하게 활용할 수 있습니다.

---


## 📦 설치 방법

**Gradle 설정 예시**

```groovy
dependencies {
    implementation("io.github.freegrowenterprise:SpaceSDK-Android:0.0.3")
}
```

---

## ✅ 주요 기능

- BLE + UWB 기반 거리 측정 (Ranging)
- RTLS 기반 위치 추정 (x, y, z 계산)
- 실시간 디바이스 연결/해제 콜백

---

## 🔧 요구 사항
- [UWB 지원 Android 휴대폰](https://blog.naver.com/growdevelopers/223812647964)
- Android 14 (API 34) 이상
- Kotlin 1.9.22 (권장)
- 실제 UWB 디바이스 **(Grow Space UWB 제품)**

---

## 📑 Android 권한 설정
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.UWB_RANGING"/>
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 🧱 초기화

```kotlin
val spaceUwb = SpaceUwb(context = applicationContext, activity = this)
```

## 🚀 거리 측정 시작
```kotlin
spaceUwb.startUwbRanging(
    maximumConnectionCount = 4,
    replacementDistanceThreshold = 8f,
    isConnectStrongestSignalFirst = true,
    onUpdate = { uwbRange ->
        Log.d("UWB", "deviceName: ${result.deviceName} distance: ${result.distance}m, azimuth: ${result.azimuth}, elevation: ${result.elevation}")
    },
    onDisconnect = { disconnect ->
        Log.w("UWB", "❌ 연결 해제: ${disconnect.deviceName}")
    }
)
```

## 🛑 거리 측정 중지
```kotlin
spaceUwb.stopUwbRanging { result ->
    if (result.isSuccess) {
        Log.i("UWB", "✅ 거리 측정 종료")
    } else {
        Log.e("UWB", "❌ 종료 실패: ${result.exceptionOrNull()?.message}")
    }
}
```

## 📍 RTLS 위치 추정
```kotlin
val anchorMap = mapOf(
    // BLE 장치명 기준. 예: UWB 장치 이름이 FGU-0001로 광고되는 경우
    "FGU-0001" to Triple(0.0, 0.0, 0.0),
    "FGU-0002" to Triple(5.0, 0.0, 0.0),
    "FGU-0003" to Triple(0.0, 5.0, 0.0)
)

spaceUwb.startUwbRtls(
    anchorPositionMap = anchorMap,
    zCorrection = 1.0f,
    maximumConnectionCount = 4,
    replacementDistanceThreshold = 8f,
    isConnectStrongestSignalFirst = true,
    filterType = RtlsFilterType.AVERAGE,
    onResult = { location ->
        Log.d("RTLS", "📍 위치: x=${location.x}, y=${location.y}, z=${location.z}")
    },
    onFail = { reason ->
        Log.e("RTLS", "❌ 위치 추정 실패: $reason")
    },
    onDeviceRanging = { distanceMap ->
        distanceMap.forEach { (id, distance) ->
            Log.d("RTLS", "📡 $id → $distance m")
        }
    }
)
```

---

### 📱 테스트 앱 안내

본 SDK를 활용한 공식 테스트 앱이 아래 경로에 공개되어 있습니다.

실제 디바이스와 연동하여 UWB 거리 측정 및 RTLS 위치 추정 기능을 직접 체험할 수 있습니다.	

[GitHub](https://github.com/freegrowenterprise/SpaceSDK-Android-TestApp)

[Google Play](https://play.google.com/store/apps/details?id=com.growspace.testapp&pcampaignid=web_share)

 ---

## 🏢 제작

**FREEGROW Inc.**  
실내 측위와 근거리 무선 통신 기술을 바탕으로 한 UWB 솔루션을 개발하고 있습니다.

---

## 📫 문의

기술 문의나 개선 제안은 아래 메일로 연락주세요.

📮 contact@freegrow.io

🌐 https://grow-space.io

