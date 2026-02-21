from pydantic_settings import BaseSettings
from pydantic import Field
from typing import Literal


class Settings(BaseSettings):
    telegram_bot_token: str = Field(..., description="Telegram Bot Token from BotFather")
    
    ai_provider: Literal["openai", "anthropic"] = Field(default="openai")
    openai_api_key: str = Field(default="")
    anthropic_api_key: str = Field(default="")
    ai_model: str = Field(default="gpt-4-turbo")
    max_tokens: int = Field(default=2000)
    temperature: float = Field(default=0.7)
    
    database_url: str = Field(default="sqlite+aiosqlite:///./bot.db")
    redis_url: str = Field(default="")
    
    debug: bool = Field(default=False)
    log_level: str = Field(default="INFO")
    max_context_messages: int = Field(default=10)
    
    admin_user_ids: list[int] = Field(default_factory=list)
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        
        @classmethod
        def parse_env_var(cls, field_name: str, raw_val: str):
            if field_name == "admin_user_ids":
                return [int(x.strip()) for x in raw_val.split(",") if x.strip()]
            return raw_val


settings = Settings()
