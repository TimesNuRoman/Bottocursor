from typing import Optional
from sqlalchemy import select, delete
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker

from src.config import settings
from src.utils import get_logger
from .models import Base, User, Message

logger = get_logger(__name__)


class DatabaseRepository:
    def __init__(self):
        self.engine = create_async_engine(settings.database_url, echo=settings.debug)
        self.session_factory = async_sessionmaker(self.engine, expire_on_commit=False)
    
    async def init_db(self) -> None:
        async with self.engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        logger.info("database_initialized")
    
    async def get_or_create_user(
        self,
        user_id: int,
        username: Optional[str] = None,
        first_name: Optional[str] = None,
        last_name: Optional[str] = None,
    ) -> User:
        async with self.session_factory() as session:
            result = await session.execute(select(User).where(User.id == user_id))
            user = result.scalar_one_or_none()
            
            if user is None:
                user = User(
                    id=user_id,
                    username=username,
                    first_name=first_name,
                    last_name=last_name,
                )
                session.add(user)
                await session.commit()
                logger.info("user_created", user_id=user_id)
            else:
                user.username = username
                user.first_name = first_name
                user.last_name = last_name
                await session.commit()
            
            return user
    
    async def save_message(
        self,
        user_id: int,
        role: str,
        content: str,
        tokens_used: Optional[int] = None,
    ) -> Message:
        async with self.session_factory() as session:
            message = Message(
                user_id=user_id,
                role=role,
                content=content,
                tokens_used=tokens_used,
            )
            session.add(message)
            await session.commit()
            return message
    
    async def get_user_context(
        self,
        user_id: int,
        limit: int = None,
    ) -> list[dict]:
        limit = limit or settings.max_context_messages
        
        async with self.session_factory() as session:
            result = await session.execute(
                select(Message)
                .where(Message.user_id == user_id)
                .order_by(Message.created_at.desc())
                .limit(limit)
            )
            messages = result.scalars().all()
            
            return [
                {"role": msg.role, "content": msg.content}
                for msg in reversed(messages)
            ]
    
    async def clear_user_context(self, user_id: int) -> int:
        async with self.session_factory() as session:
            result = await session.execute(
                delete(Message).where(Message.user_id == user_id)
            )
            await session.commit()
            return result.rowcount
    
    async def get_user_settings(self, user_id: int) -> dict:
        async with self.session_factory() as session:
            result = await session.execute(select(User).where(User.id == user_id))
            user = result.scalar_one_or_none()
            return user.settings if user else {}
    
    async def update_user_settings(self, user_id: int, new_settings: dict) -> None:
        async with self.session_factory() as session:
            result = await session.execute(select(User).where(User.id == user_id))
            user = result.scalar_one_or_none()
            if user:
                user.settings = {**user.settings, **new_settings}
                await session.commit()
