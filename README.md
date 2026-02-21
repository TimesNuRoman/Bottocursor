# Simple Counter App (Простой счётчик)

Простое Android-приложение для подсчёта. Демонстрирует основы Android-разработки с использованием Kotlin и Material Design 3.

## Возможности

- Увеличение счётчика (+)
- Уменьшение счётчика (−)
- Сброс до нуля (↺)
- Анимация при изменении значения
- Цветовая индикация (зелёный для положительных, красный для отрицательных)
- Сохранение состояния при повороте экрана

## Технологии

- **Язык**: Kotlin
- **UI**: Material Design 3
- **View Binding**: Для безопасного доступа к элементам интерфейса
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Сборка проекта

### Требования

- Android Studio Arctic Fox или новее
- JDK 8 или новее
- Android SDK 34

### Запуск

1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Выберите устройство или эмулятор
4. Нажмите "Run" (Shift+F10)

### Сборка APK

```bash
./gradlew assembleDebug
```

APK будет находиться в `app/build/outputs/apk/debug/`

## Структура проекта

```
app/
├── src/main/
│   ├── java/com/example/simpleapp/
│   │   └── MainActivity.kt          # Главная активность
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml    # Макет интерфейса
│   │   ├── values/
│   │   │   ├── colors.xml           # Цвета
│   │   │   ├── strings.xml          # Строки
│   │   │   └── themes.xml           # Темы
│   │   ├── anim/
│   │   │   └── scale_pulse.xml      # Анимация
│   │   └── drawable/                # Иконки
│   └── AndroidManifest.xml
└── build.gradle.kts                 # Конфигурация модуля
```

## Скриншот

Приложение представляет собой экран с крупным числом в центре (счётчик) и тремя круглыми кнопками внизу для управления.

## Лицензия

MIT License
