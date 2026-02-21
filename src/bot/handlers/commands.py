from aiogram import Router, types
from aiogram.filters import Command, CommandStart

from src.database import DatabaseRepository
from src.utils import get_logger

router = Router()
logger = get_logger(__name__)

db = DatabaseRepository()


@router.message(CommandStart())
async def cmd_start(message: types.Message):
    user = message.from_user
    await db.get_or_create_user(
        user_id=user.id,
        username=user.username,
        first_name=user.first_name,
        last_name=user.last_name,
    )
    
    logger.info("user_started", user_id=user.id, username=user.username)
    
    await message.answer(
        f"👋 Привет, {user.first_name}!\n\n"
        "Я — AI-ассистент. Просто напиши мне сообщение, "
        "и я отвечу с помощью искусственного интеллекта.\n\n"
        "📝 **Команды:**\n"
        "/help — Справка по командам\n"
        "/clear — Очистить контекст диалога\n"
        "/settings — Настройки бота",
        parse_mode="Markdown"
    )


@router.message(Command("help"))
async def cmd_help(message: types.Message):
    await message.answer(
        "🤖 **AI Telegram Bot**\n\n"
        "Я могу помочь тебе с различными задачами:\n"
        "• Ответы на вопросы\n"
        "• Помощь с программированием\n"
        "• Генерация текстов\n"
        "• Перевод и редактирование\n"
        "• И многое другое!\n\n"
        "📝 **Команды:**\n"
        "/start — Начать диалог\n"
        "/clear — Очистить историю диалога\n"
        "/settings — Настройки\n"
        "/help — Эта справка\n\n"
        "💡 Просто напиши своё сообщение, и я отвечу!",
        parse_mode="Markdown"
    )


@router.message(Command("clear"))
async def cmd_clear(message: types.Message):
    user_id = message.from_user.id
    deleted_count = await db.clear_user_context(user_id)
    
    logger.info("context_cleared", user_id=user_id, messages_deleted=deleted_count)
    
    await message.answer(
        f"🗑️ Контекст очищен!\n"
        f"Удалено сообщений: {deleted_count}\n\n"
        "Теперь можешь начать новый диалог."
    )


@router.message(Command("settings"))
async def cmd_settings(message: types.Message):
    user_id = message.from_user.id
    user_settings = await db.get_user_settings(user_id)
    
    prompt_type = user_settings.get("prompt_type", "default")
    
    await message.answer(
        "⚙️ **Настройки**\n\n"
        f"📌 Режим ассистента: `{prompt_type}`\n\n"
        "Доступные режимы:\n"
        "• `default` — Универсальный помощник\n"
        "• `coding` — Помощь с программированием\n"
        "• `creative` — Креативное письмо\n\n"
        "Чтобы сменить режим, напиши:\n"
        "`/mode coding`",
        parse_mode="Markdown"
    )


@router.message(Command("mode"))
async def cmd_mode(message: types.Message):
    user_id = message.from_user.id
    args = message.text.split(maxsplit=1)
    
    valid_modes = ["default", "coding", "creative"]
    
    if len(args) < 2:
        await message.answer(
            "⚠️ Укажи режим:\n"
            "`/mode default` | `/mode coding` | `/mode creative`",
            parse_mode="Markdown"
        )
        return
    
    mode = args[1].lower().strip()
    
    if mode not in valid_modes:
        await message.answer(
            f"❌ Неизвестный режим: `{mode}`\n\n"
            f"Доступные режимы: {', '.join(valid_modes)}",
            parse_mode="Markdown"
        )
        return
    
    await db.update_user_settings(user_id, {"prompt_type": mode})
    
    mode_descriptions = {
        "default": "Универсальный помощник",
        "coding": "Помощь с программированием",
        "creative": "Креативное письмо",
    }
    
    await message.answer(
        f"✅ Режим изменён на: **{mode}**\n"
        f"_{mode_descriptions[mode]}_",
        parse_mode="Markdown"
    )
    
    logger.info("mode_changed", user_id=user_id, mode=mode)
