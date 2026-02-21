# 🤖 Telegram AI Bot

## Обзор проекта

Telegram-бот с интеграцией искусственного интеллекта для интеллектуального общения с пользователями.

---

## 📋 План разработки

### Фаза 1: Инфраструктура и базовая настройка

- [ ] Инициализация проекта (Python + Poetry/pip)
- [ ] Настройка структуры папок
- [ ] Конфигурация переменных окружения
- [ ] Настройка логирования
- [ ] Docker-контейнеризация

### Фаза 2: Telegram Bot Core

- [ ] Регистрация бота через @BotFather
- [ ] Интеграция python-telegram-bot / aiogram
- [ ] Базовые команды (/start, /help, /settings)
- [ ] Обработка текстовых сообщений
- [ ] Обработка ошибок и retry-логика

### Фаза 3: AI Интеграция

- [ ] Интеграция OpenAI API / Anthropic Claude
- [ ] Система промптов и контекста
- [ ] Управление историей диалога
- [ ] Rate limiting и квоты
- [ ] Fallback при недоступности AI

### Фаза 4: Дополнительные функции

- [ ] База данных для хранения истории (PostgreSQL/SQLite)
- [ ] Пользовательские настройки и профили
- [ ] Поддержка голосовых сообщений (Speech-to-Text)
- [ ] Генерация изображений (DALL-E / Stable Diffusion)
- [ ] Inline-режим бота

### Фаза 5: Деплой и мониторинг

- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Деплой на VPS / Cloud (Railway, Render, VPS)
- [ ] Мониторинг и алерты
- [ ] Документация API

---

## 🏗️ Архитектура

```
telegram-ai-bot/
├── src/
│   ├── __init__.py
│   ├── main.py              # Точка входа
│   ├── bot/
│   │   ├── __init__.py
│   │   ├── handlers/        # Обработчики команд и сообщений
│   │   │   ├── __init__.py
│   │   │   ├── commands.py  # /start, /help, /settings
│   │   │   └── messages.py  # Обработка текста
│   │   ├── keyboards/       # Inline и Reply клавиатуры
│   │   │   └── __init__.py
│   │   └── middlewares/     # Middleware (auth, logging)
│   │       └── __init__.py
│   ├── ai/
│   │   ├── __init__.py
│   │   ├── client.py        # AI клиент (OpenAI/Anthropic)
│   │   ├── prompts.py       # Системные промпты
│   │   └── context.py       # Управление контекстом диалога
│   ├── database/
│   │   ├── __init__.py
│   │   ├── models.py        # SQLAlchemy модели
│   │   └── repository.py    # CRUD операции
│   ├── config/
│   │   ├── __init__.py
│   │   └── settings.py      # Pydantic Settings
│   └── utils/
│       ├── __init__.py
│       └── logger.py        # Настройка логирования
├── tests/
│   ├── __init__.py
│   ├── test_handlers.py
│   └── test_ai.py
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── pyproject.toml
├── requirements.txt
└── README.md
```

---

## 🛠️ Технологический стек

| Компонент | Технология | Описание |
|-----------|------------|----------|
| **Язык** | Python 3.11+ | Основной язык разработки |
| **Telegram API** | aiogram 3.x | Асинхронный фреймворк для Telegram |
| **AI Provider** | OpenAI / Anthropic | GPT-4 / Claude для генерации ответов |
| **База данных** | PostgreSQL + SQLAlchemy | Хранение истории и настроек |
| **Кэширование** | Redis | Кэш сессий и rate limiting |
| **Конфигурация** | Pydantic Settings | Типизированные настройки |
| **Контейнеризация** | Docker + Docker Compose | Изоляция и деплой |
| **CI/CD** | GitHub Actions | Автоматизация тестов и деплоя |

---

## 🔧 Конфигурация

### Переменные окружения (.env)

```env
# Telegram
TELEGRAM_BOT_TOKEN=your_bot_token_here

# AI Provider
AI_PROVIDER=openai  # или anthropic
OPENAI_API_KEY=your_openai_key
ANTHROPIC_API_KEY=your_anthropic_key

# Database
DATABASE_URL=postgresql://user:password@localhost:5432/telegram_bot

# Redis
REDIS_URL=redis://localhost:6379/0

# App Settings
DEBUG=false
LOG_LEVEL=INFO
MAX_CONTEXT_MESSAGES=10
AI_MODEL=gpt-4-turbo
MAX_TOKENS=2000
```

---

## 📝 Примеры кода

### Базовый обработчик команды /start

```python
from aiogram import Router, types
from aiogram.filters import Command

router = Router()

@router.message(Command("start"))
async def cmd_start(message: types.Message):
    await message.answer(
        "👋 Привет! Я AI-бот.\n\n"
        "Просто напиши мне сообщение, и я отвечу с помощью ИИ.\n\n"
        "Команды:\n"
        "/help - Помощь\n"
        "/settings - Настройки\n"
        "/clear - Очистить контекст"
    )
```

### AI клиент

```python
from openai import AsyncOpenAI
from src.config import settings

class AIClient:
    def __init__(self):
        self.client = AsyncOpenAI(api_key=settings.openai_api_key)
    
    async def generate_response(
        self, 
        messages: list[dict],
        user_message: str
    ) -> str:
        messages.append({"role": "user", "content": user_message})
        
        response = await self.client.chat.completions.create(
            model=settings.ai_model,
            messages=messages,
            max_tokens=settings.max_tokens,
            temperature=0.7
        )
        
        return response.choices[0].message.content
```

### Обработчик сообщений с AI

```python
from aiogram import Router, types
from src.ai import AIClient
from src.database import get_user_context, save_message

router = Router()
ai_client = AIClient()

@router.message()
async def handle_message(message: types.Message):
    user_id = message.from_user.id
    
    # Получаем контекст диалога
    context = await get_user_context(user_id)
    
    # Генерируем ответ
    response = await ai_client.generate_response(
        messages=context,
        user_message=message.text
    )
    
    # Сохраняем в БД
    await save_message(user_id, message.text, response)
    
    await message.answer(response)
```

---

## 🚀 Быстрый старт

### 1. Клонирование и установка

```bash
git clone <repository-url>
cd telegram-ai-bot

# Создание виртуального окружения
python -m venv venv
source venv/bin/activate  # Linux/Mac
# или venv\Scripts\activate  # Windows

# Установка зависимостей
pip install -r requirements.txt
```

### 2. Настройка окружения

```bash
cp .env.example .env
# Отредактируйте .env, добавив свои токены
```

### 3. Запуск

```bash
# Локально
python -m src.main

# Через Docker
docker-compose up -d
```

---

## 🐳 Docker

### Dockerfile

```dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY src/ ./src/

CMD ["python", "-m", "src.main"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  bot:
    build: .
    env_file: .env
    restart: unless-stopped
    depends_on:
      - db
      - redis

  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: bot
      POSTGRES_PASSWORD: password
      POSTGRES_DB: telegram_bot
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

---

## 📊 Схема базы данных

```sql
-- Пользователи
CREATE TABLE users (
    id BIGINT PRIMARY KEY,  -- Telegram user_id
    username VARCHAR(255),
    first_name VARCHAR(255),
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- История сообщений
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    role VARCHAR(20) NOT NULL,  -- 'user' или 'assistant'
    content TEXT NOT NULL,
    tokens_used INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Индексы
CREATE INDEX idx_messages_user_id ON messages(user_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
```

---

## 🔒 Безопасность

1. **Хранение секретов** - Все токены в переменных окружения
2. **Rate Limiting** - Ограничение запросов на пользователя
3. **Валидация входных данных** - Проверка длины сообщений
4. **Логирование** - Без записи чувствительных данных
5. **Whitelist/Blacklist** - Управление доступом пользователей

---

## 📈 Мониторинг

### Метрики для отслеживания

- Количество активных пользователей
- Среднее время ответа AI
- Использование токенов
- Ошибки и их типы
- Uptime бота

### Инструменты

- **Sentry** - Отслеживание ошибок
- **Prometheus + Grafana** - Метрики
- **Telegram Alerts** - Уведомления администратору

---

## 📚 Ресурсы

- [aiogram Documentation](https://docs.aiogram.dev/)
- [OpenAI API Reference](https://platform.openai.com/docs/api-reference)
- [Anthropic Claude API](https://docs.anthropic.com/)
- [Telegram Bot API](https://core.telegram.org/bots/api)

---

## 📄 Лицензия

MIT License
