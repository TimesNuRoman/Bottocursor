import asyncio
from aiogram import Bot, Dispatcher
from aiogram.client.default import DefaultBotProperties
from aiogram.enums import ParseMode

from src.config import settings
from src.utils import setup_logging, get_logger
from src.database import DatabaseRepository
from src.bot.handlers import commands_router, messages_router

logger = get_logger(__name__)


async def main():
    setup_logging()
    
    logger.info("starting_bot", debug=settings.debug)
    
    db = DatabaseRepository()
    await db.init_db()
    
    bot = Bot(
        token=settings.telegram_bot_token,
        default=DefaultBotProperties(parse_mode=ParseMode.MARKDOWN),
    )
    
    dp = Dispatcher()
    
    dp.include_router(commands_router)
    dp.include_router(messages_router)
    
    logger.info("bot_started")
    
    try:
        await dp.start_polling(bot)
    finally:
        await bot.session.close()
        logger.info("bot_stopped")


if __name__ == "__main__":
    asyncio.run(main())
