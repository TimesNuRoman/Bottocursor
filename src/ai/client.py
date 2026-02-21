from abc import ABC, abstractmethod
from typing import Optional
from openai import AsyncOpenAI
from anthropic import AsyncAnthropic

from src.config import settings
from src.utils import get_logger
from .prompts import SystemPrompts

logger = get_logger(__name__)


class BaseAIClient(ABC):
    @abstractmethod
    async def generate_response(
        self,
        messages: list[dict],
        user_message: str,
        system_prompt: Optional[str] = None,
    ) -> str:
        pass


class OpenAIClient(BaseAIClient):
    def __init__(self):
        self.client = AsyncOpenAI(api_key=settings.openai_api_key)
        self.model = settings.ai_model
    
    async def generate_response(
        self,
        messages: list[dict],
        user_message: str,
        system_prompt: Optional[str] = None,
    ) -> str:
        system = system_prompt or SystemPrompts.DEFAULT
        
        full_messages = [{"role": "system", "content": system}]
        full_messages.extend(messages)
        full_messages.append({"role": "user", "content": user_message})
        
        try:
            response = await self.client.chat.completions.create(
                model=self.model,
                messages=full_messages,
                max_tokens=settings.max_tokens,
                temperature=settings.temperature,
            )
            return response.choices[0].message.content
        except Exception as e:
            logger.error("openai_error", error=str(e))
            raise


class AnthropicClient(BaseAIClient):
    def __init__(self):
        self.client = AsyncAnthropic(api_key=settings.anthropic_api_key)
        self.model = settings.ai_model if "claude" in settings.ai_model else "claude-3-5-sonnet-20241022"
    
    async def generate_response(
        self,
        messages: list[dict],
        user_message: str,
        system_prompt: Optional[str] = None,
    ) -> str:
        system = system_prompt or SystemPrompts.DEFAULT
        
        full_messages = list(messages)
        full_messages.append({"role": "user", "content": user_message})
        
        try:
            response = await self.client.messages.create(
                model=self.model,
                max_tokens=settings.max_tokens,
                system=system,
                messages=full_messages,
            )
            return response.content[0].text
        except Exception as e:
            logger.error("anthropic_error", error=str(e))
            raise


class AIClient:
    def __init__(self):
        if settings.ai_provider == "anthropic":
            self._client = AnthropicClient()
        else:
            self._client = OpenAIClient()
        
        logger.info("ai_client_initialized", provider=settings.ai_provider)
    
    async def generate_response(
        self,
        messages: list[dict],
        user_message: str,
        system_prompt: Optional[str] = None,
    ) -> str:
        return await self._client.generate_response(
            messages=messages,
            user_message=user_message,
            system_prompt=system_prompt,
        )
