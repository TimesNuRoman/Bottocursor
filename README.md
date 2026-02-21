# Simple App - Android

Простое Android-приложение с счётчиком нажатий.

## Функции

- Счётчик нажатий с красивым UI
- Кнопка сброса счётчика
- Toast-уведомления при достижении каждых 10 нажатий
- Material Design 3

## Требования

- Android Studio Arctic Fox или новее
- Android SDK 34
- Minimum SDK: 24 (Android 7.0)
- Kotlin 1.9.20
- Gradle 8.2

## Сборка

1. Откройте проект в Android Studio
2. Синхронизируйте Gradle
3. Запустите на эмуляторе или устройстве

```bash
./gradlew assembleDebug
```

## Структура проекта

```
app/
├── src/main/
│   ├── java/com/example/simpleapp/
│   │   └── MainActivity.kt
│   ├── res/
│   │   ├── layout/activity_main.xml
│   │   ├── values/
│   │   │   ├── strings.xml
│   │   │   ├── colors.xml
│   │   │   └── themes.xml
│   │   └── drawable/
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Скриншот

Приложение показывает:
- Заголовок "Simple App"
- Приветственное сообщение
- Карточка со счётчиком
- Кнопка "Нажми меня!"
- Кнопка "Сбросить"
