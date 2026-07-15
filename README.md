# InterviewIQ

AI-powered interview preparation assistant.

This repository is being built in six independently testable phases. The current implementation includes the core portfolio flow:

- Authentication with JWT access tokens and rotating HttpOnly refresh cookies
- Resume PDF upload, validation, storage, text extraction, and soft delete
- Job description creation from pasted text or uploaded PDF
- Async report generation with persisted status and AI usage logs
- Dashboard aggregation and report history
- Report-scoped chat sessions with persisted messages
- Redis in Docker Compose for the Phase 5 cache/rate-limiting path

The default AI provider is `local`, a deterministic provider that lets the full workflow run without paid API credentials. The Ollama provider is backed by Spring AI and still sits behind the app-level `AiClient` interface.

## Supported AI Providers

You can switch the backend AI provider by setting `APP_AI_PROVIDER` in your `.env` to one of:

- `local` - deterministic, default for development
- `openai` - existing OpenAI Responses provider (requires `OPENAI_API_KEY`)
- `ollama` - Spring AI-backed local Ollama server (recommended for a self-hosted Llama 3.1 setup)
- `gemini` - Google Gemini Interactions API provider (requires `GEMINI_API_KEY`)

Example: enable Gemini:

```bash
APP_AI_PROVIDER=gemini
GEMINI_BASE_URL=https://generativelanguage.googleapis.com/v1beta
GEMINI_API_KEY=your-google-ai-studio-key
GEMINI_MODEL=gemini-3.5-flash
```

Google AI Studio may issue either standard API keys or newer auth keys. Use the exact key AI Studio gives you in `GEMINI_API_KEY`.

Example: enable Ollama with Llama 3.1:

```bash
APP_AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=llama3.1:8b
```

Install Ollama on the host machine, then pull the model:

```bash
ollama pull llama3.1:8b
```

Then restart:

```bash
docker compose up -d --build
```

## Local Development

1. Copy `.env.example` to `.env`.
2. Run:

```bash
docker compose up --build
```

Backend: `http://localhost:8080/api/v1/health`

Frontend: `http://localhost:5173`

## Local Build Checks

If Maven is installed:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm install
npm run build
```
