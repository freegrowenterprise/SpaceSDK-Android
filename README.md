# 📡 SpaceSDK-Android

**SpaceSDK** is a UWB-based Android SDK developed by **FREEGROW Inc.**, providing features such as distance measurement, direction calculation, and real-time RTLS (location estimation).  
With a single class `SpaceUwb`, developers can easily utilize UWB functionality without dealing with complex connection workflows.

---

## 📦 Installation

**Gradle setup example:**

```groovy
dependencies {
    implementation("io.github.freegrowenterprise:SpaceSDK-Android:0.0.4")
}
```

---

## ✅ Key Features
- BLE + UWB-based distance measurement (Ranging)
- RTLS-based real-time location estimation (x, y, z)
- Real-time device connection/disconnection callbacks

---

## 🔧 Requirements
- [UWB-supported Android device](https://blog.naver.com/growdevelopers/223812647964)
- Android 14 (API 34) or later
- Kotlin 1.9.22 (recommended)
- Physical UWB device (Grow Space UWB product)

---

## 📑 Android Permission Configuration

Add the following to your AndroidManifest.xml:

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

## 🧱 Initialization

```kotlin
val spaceUwb = SpaceUwb(context = applicationContext, activity = this)
```

## 🚀 Start Ranging
```kotlin
spaceUwb.startUwbRanging(
    maximumConnectionCount = 4,
    replacementDistanceThreshold = 8f,
    isConnectStrongestSignalFirst = true,
    onUpdate = { uwbRange ->
        Log.d("UWB", "deviceName: ${result.deviceName} distance: ${result.distance}m, azimuth: ${result.azimuth}, elevation: ${result.elevation}")
    },
    onDisconnect = { disconnect ->
        Log.w("UWB", "❌ Disconnected: ${disconnect.deviceName}")
    }
)
```

## 🛑 Stop Ranging
```kotlin
spaceUwb.stopUwbRanging { result ->
    if (result.isSuccess) {
        Log.i("UWB", "✅ Ranging stopped")
    } else {
        Log.e("UWB", "❌ Failed to stop ranging: ${result.exceptionOrNull()?.message}")
    }
}
```

## 📍 RTLS Location Estimation
```kotlin
val anchorMap = mapOf(
    // Based on BLE device names (e.g., UWB device advertises as FGU-0001)
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
        Log.d("RTLS", "📍 Location: x=${location.x}, y=${location.y}, z=${location.z}")
    },
    onFail = { reason ->
        Log.e("RTLS", "❌ Location estimation failed: $reason")
    },
    onDeviceRanging = { distanceMap ->
        distanceMap.forEach { (id, distance) ->
            Log.d("RTLS", "📡 $id → $distance m")
        }
    }
)
```

---

### 📱 Test App

An official test app built with this SDK is publicly available at the links below.
It allows you to try out UWB ranging and RTLS features with actual devices.

[GitHub](https://github.com/freegrowenterprise/SpaceSDK-Android-TestApp)

[Google Play](https://play.google.com/store/apps/details?id=com.growspace.testapp&pcampaignid=web_share)

 ---

## 🏢 Developed by

**FREEGROW Inc.**  
We specialize in indoor positioning and ultra-wideband (UWB) communication technologies to enable intelligent spatial awareness solutions.


---

## 📫 Contact

For technical support or suggestions, feel free to contact us:

📮 contact@freegrow.io

🌐 https://grow-space.io

