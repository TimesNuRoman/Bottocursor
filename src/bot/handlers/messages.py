from aiogram import Router, types, F

from src.ai import AIClient, SystemPrompts
from src.database import DatabaseRepository
from src.utils import get_logger

router = Router()
logger = get_logger(__name__)

db = DatabaseRepository()
ai_client = AIClient()


@router.message(F.text)
async def handle_text_message(message: types.Message):
    user = message.from_user
    user_id = user.id
    user_text = message.text
    
    await db.get_or_create_user(
        user_id=user_id,
        username=user.username,
        first_name=user.first_name,
        last_name=user.last_name,
    )
    
    typing_task = await message.answer("⏳ Думаю...")
    
    try:
        context = await db.get_user_context(user_id)
        
        user_settings = await db.get_user_settings(user_id)
        prompt_type = user_settings.get("prompt_type", "default")
        system_prompt = SystemPrompts.get_prompt(prompt_type)
        
        response = await ai_client.generate_response(
            messages=context,
            user_message=user_text,
            system_prompt=system_prompt,
        )
        
        await db.save_message(user_id, "user", user_text)
        await db.save_message(user_id, "assistant", response)
        
        await typing_task.delete()
        
        if len(response) > 4096:
            for i in range(0, len(response), 4096):
                await message.answer(response[i:i+4096], parse_mode="Markdown")
        else:
            await message.answer(response, parse_mode="Markdown")
        
        logger.info(
            "message_processed",
            user_id=user_id,
            input_length=len(user_text),
            output_length=len(response),
        )
        
    except Exception as e:
        logger.error("message_processing_error", user_id=user_id, error=str(e))
        await typing_task.delete()
        await message.answer(
            "😔 Произошла ошибка при обработке сообщения.\n"
            "Пожалуйста, попробуй позже или используй /clear для сброса контекста."
        )


@router.message(F.photo)
async def handle_photo(message: types.Message):
    await message.answer(
        "🖼️ Пока я не умею обрабатывать изображения.\n"
        "Опиши словами, что тебя интересует!"
    )


@router.message(F.voice)
async def handle_voice(message: types.Message):
    await message.answer(
        "🎤 Голосовые сообщения пока не поддерживаются.\n"
        "Напиши текстом, и я с радостью помогу!"
    )


@router.message()
async def handle_unknown(message: types.Message):
    await message.answer(
        "🤔 Я пока не умею обрабатывать такой тип сообщений.\n"
        "Попробуй написать текстовое сообщение!"
    )
