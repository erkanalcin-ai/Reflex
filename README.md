# Reflex

Reflex, Android için geliştirilen basit bir refleks oyunudur.

## Status

Development completed.

Bu repository, mevcut release sürümünün kaynak kodunu içerir.

## Android

- Application ID: `com.erkan.reflex`
- compileSdk: 36
- targetSdk: 36
- versionName: 1.0
- versionCode: 1

## Build

Windows:
```powershell
.\gradlew.bat assembleDebug
```

Release:
```powershell
.\gradlew.bat assembleRelease
```

Production release için signing yapılandırması gerekir.

## Signing

Production keystore ve gerçek signing bilgileri repository'de tutulmaz.

`key.properties.example` yalnızca örnek yapılandırmadır.

GERÇEK `key.properties` ve keystore dosyaları Git'e eklenmemelidir.
