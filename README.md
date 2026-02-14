# Cursor AI Remote

**Android-приложение для удалённого управления Cursor AI с помощью голосового ввода**

Modern Android app that lets you remotely control [Cursor AI](https://cursor.sh) IDE using voice commands and a beautiful touch interface. Works on phones and tablets.

---

## Features

### Voice Control
- **Speech-to-text** voice input with real-time waveform visualization
- **Multi-language** support (English, Russian, German, French, Spanish, Japanese, Chinese, Korean)
- **Smart command mapping** — natural language commands are mapped to Cursor IDE actions
- **Quick actions** — one-tap shortcuts for common commands

### IDE-like Interface
- **Code editor** with syntax highlighting and line numbers
- **File explorer** with collapsible tree structure
- **Integrated terminal** with command input
- **AI Chat panel** with streaming responses
- **Editor toolbar** with common actions (undo, redo, copy, paste, find, format, save)

### Tablet Support
- **Adaptive layout** — automatically switches between phone and tablet UI
- **Navigation Rail** on tablets (side navigation)
- **Split panels** — editor + file tree, chat + editor side-by-side
- **Responsive design** using Material 3 WindowSizeClass

### Modern Design
- **Material 3** design system with dark theme (Cursor-inspired)
- **Smooth animations** — voice pulse, content transitions, streaming indicators
- **Custom color scheme** — dark IDE aesthetic with purple/cyan accents
- **Edge-to-edge** rendering with transparent status/navigation bars

---

## Architecture

```
┌─────────────────────────┐         ┌──────────────────────────┐
│   Android App (Client)  │◄──WSS──►│  Companion Server (PC)   │
│                         │         │                          │
│  ┌──────────────────┐   │         │  ┌────────────────────┐  │
│  │  Voice Input      │   │         │  │  WebSocket Server   │  │
│  │  (SpeechRecognizer)│   │         │  │  (Node.js / ws)     │  │
│  └────────┬─────────┘   │         │  └─────────┬──────────┘  │
│           │              │         │            │              │
│  ┌────────▼─────────┐   │         │  ┌─────────▼──────────┐  │
│  │  Command Mapper   │   │         │  │  Cursor CLI Bridge  │  │
│  │  (VoiceCommandMap)│   │         │  │  (exec commands)    │  │
│  └────────┬─────────┘   │         │  └─────────┬──────────┘  │
│           │              │         │            │              │
│  ┌────────▼─────────┐   │         │  ┌─────────▼──────────┐  │
│  │  WebSocket Client │   │         │  │  Cursor IDE         │  │
│  │  (OkHttp)         │   │         │  │  (running locally)  │  │
│  └──────────────────┘   │         │  └────────────────────┘  │
└─────────────────────────┘         └──────────────────────────┘
```

---

## Getting Started

### 1. Start the Companion Server (on your PC)

```bash
cd server
npm install
npm start
```

By default, the server runs on port `9090`. Configure with environment variables:

```bash
PORT=9090 AUTH_TOKEN=mysecret npm start
```

### 2. Install the Android App

Build the APK:

```bash
./gradlew assembleDebug
```

Or install directly to a connected device:

```bash
./gradlew installDebug
```

### 3. Connect

1. Open the app on your Android device
2. Go to **Settings**
3. Enter your PC's IP address and port (default: 9090)
4. Tap **Connect**

**Important:** Both devices must be on the same network.

---

## Voice Commands

### English

| Command | Action |
|---------|--------|
| "Open file" | Quick Open dialog |
| "Save" / "Save all" | Save current / all files |
| "Close tab" | Close active editor |
| "Undo" / "Redo" | Undo / Redo |
| "Find" / "Replace" | Find / Find & Replace |
| "Format" | Format document |
| "Open terminal" | Toggle terminal |
| "Run" / "Debug" / "Stop" | Run/Debug controls |
| "Git commit/push/pull" | Git operations |
| "Compose" / "AI Chat" | Cursor AI features |
| "Command palette" | Open command palette |

### Русский

| Команда | Действие |
|---------|----------|
| "Открой файл" | Быстрое открытие |
| "Сохрани" / "Сохрани все" | Сохранить файл(ы) |
| "Закрой вкладку" | Закрыть вкладку |
| "Отмени" / "Повтори" | Отмена / Повтор |
| "Найди" / "Замени" | Поиск / Замена |
| "Форматируй" | Форматирование |
| "Терминал" | Открыть терминал |
| "Запусти" / "Дебаг" / "Стоп" | Управление запуском |
| "Коммит" / "Пуш" / "Пул" | Git операции |
| "Композер" / "Чат" | Cursor AI |
| "Палитра команд" | Палитра команд |

If the voice input doesn't match a known command, it's sent as an **AI prompt** to Cursor.

---

## Tech Stack

### Android App
- **Kotlin** — primary language
- **Jetpack Compose** — declarative UI framework
- **Material 3** — design system with adaptive layouts
- **OkHttp** — WebSocket client
- **Android SpeechRecognizer** — voice input
- **DataStore** — persistent settings
- **Coroutines + Flow** — async state management

### Companion Server
- **Node.js** — runtime
- **ws** — WebSocket library
- **Cursor CLI** — IDE command bridge

---

## Project Structure

```
├── app/
│   └── src/main/
│       ├── java/com/cursorai/remote/
│       │   ├── data/
│       │   │   ├── model/       # Data models
│       │   │   ├── network/     # WebSocket manager
│       │   │   └── repository/  # Settings persistence
│       │   ├── service/         # Voice & connection services
│       │   ├── ui/
│       │   │   ├── theme/       # Material 3 theme
│       │   │   ├── components/  # Reusable UI components
│       │   │   └── screens/     # App screens
│       │   ├── util/            # Voice command mapper
│       │   ├── CursorAIRemoteApp.kt
│       │   └── MainActivity.kt
│       ├── res/                 # Android resources
│       └── AndroidManifest.xml
├── server/
│   ├── server.js               # Companion WebSocket server
│   └── package.json
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## Configuration

### Connection Settings
| Setting | Default | Description |
|---------|---------|-------------|
| Host | 192.168.1.100 | PC's local IP address |
| Port | 9090 | WebSocket server port |
| TLS | Off | Enable WSS (secure) |
| Auth Token | (empty) | Optional authentication |
| Auto Connect | Off | Connect on app launch |

### Server Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 9090 | Server listen port |
| `AUTH_TOKEN` | (none) | Required auth token |
| `CURSOR_PATH` | (auto) | Path to Cursor CLI |
| `WORKSPACE` | (cwd) | Default workspace |

---

## Building

### Requirements
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Node.js 18+ (for server)

### Debug Build
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
./gradlew assembleRelease
```

---

## License

MIT License. See LICENSE file for details.
