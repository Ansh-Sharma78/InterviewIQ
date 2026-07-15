# InterviewIQ

InterviewIQ is an AI-powered interview preparation platform that compares a candidate resume against a target job description and generates a structured preparation report. It also provides report-scoped chat so users can ask follow-up questions about the generated analysis.

The current implementation is a full-stack Spring Boot + React application with a provider-agnostic AI layer. The Ollama provider is integrated through Spring AI and can run a self-hosted Llama 3.1 model locally.

## Tech Stack

- Backend: Spring Boot, Spring Security, Spring Data JPA, Flyway, MySQL
- AI: Spring AI, Ollama, Llama 3.1, provider-agnostic `AiClient`
- Frontend: React, Redux Toolkit, React Router, Tailwind CSS, Lucide React
- Infrastructure: Docker Compose, MySQL, Redis

## Core Features

- JWT authentication with HMAC-signed access tokens
- HttpOnly refresh token rotation with reuse detection
- Repository-level user ownership scoping for resumes, job descriptions, reports, and chats
- Resume PDF upload, validation, storage, and text extraction
- Job description creation from pasted text or uploaded PDF
- One-step report generation flow from resume upload + job description input
- Asynchronous report generation with persisted `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` states
- LLM JSON parsing with retry/repair for malformed report payloads
- AI usage logging for report generation and chat
- Report detail page with generated report, source resume, and source job description
- Report-scoped AI chat with an 8-message sliding-window memory
- Dark, minimal React UI
- Docker Compose setup for backend, frontend, MySQL, and Redis

## Architecture

```text
React frontend
  -> Spring Boot REST API
  -> Service layer
  -> AiClient interface
      -> LocalDeterministicAiClient
      -> OllamaClient
          -> Spring AI ChatClient
          -> Ollama
          -> llama3.1:8b
      -> OpenAiResponsesClient
      -> GeminiClient
  -> MySQL via Spring Data JPA
```

Report generation runs asynchronously:

```text
POST /reports
  -> create report row as PENDING
  -> after transaction commit, run @Async job
  -> mark PROCESSING
  -> call selected AiClient
  -> persist normalized JSON report payload and scores
  -> mark COMPLETED or FAILED
```

Chat uses report context plus recent conversation memory:

```text
ChatService
  -> loads report JSON, resume text, job description text
  -> loads latest 8 chat messages
  -> calls selected AiClient
  -> persists assistant response
```

## AI Providers

The backend selects the AI provider with:

```env
APP_AI_PROVIDER=local
```

Supported values:

- `local` - deterministic development provider; no external model required
- `ollama` - Spring AI-backed Ollama provider; recommended for local Llama 3.1
- `openai` - OpenAI Responses API provider
- `gemini` - Google Gemini provider

### Ollama / Llama 3.1

Install Ollama on the host machine, then pull the model:

```powershell
ollama pull llama3.1:8b
```

Set these values in `.env`:

```env
APP_AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=llama3.1:8b
```

`host.docker.internal` is used because the backend runs inside Docker while Ollama runs on the host machine.

## Local Setup

### 1. Create Environment File

Copy the example environment file:

```powershell
Copy-Item .env.example .env
```

For a no-LLM development run, keep:

```env
APP_AI_PROVIDER=local
```

For local Llama 3.1, use the Ollama settings above.

### 2. Start Docker Compose

```powershell
docker compose up -d --build
```

Default URLs:

- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/api/v1/health`
- MySQL: `localhost:3307` from `.env.example`
- Redis: `localhost:6379`

If you change `BACKEND_PORT` or `VITE_API_BASE_URL` in `.env`, use those values instead.

### 3. Stop Services

```powershell
docker compose down
```

To remove database data as well:

```powershell
docker compose down -v
```

## Environment Variables

Important variables from `.env.example`:

```env
MYSQL_DATABASE=interviewiq
MYSQL_USER=interviewiq
MYSQL_PASSWORD=interviewiq
MYSQL_ROOT_PASSWORD=root
MYSQL_PORT=3307

JWT_SECRET=change-me-to-a-long-random-secret-at-least-64-chars-for-dev-only
JWT_ACCESS_TOKEN_MINUTES=15
JWT_REFRESH_TOKEN_DAYS=14

BACKEND_PORT=8080
FRONTEND_PORT=5173
VITE_API_BASE_URL=http://localhost:8080/api/v1
CORS_ALLOWED_ORIGINS=http://localhost:[*],http://127.0.0.1:[*]

APP_AI_PROVIDER=local
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=llama3.1:8b
```

For production-like use, replace `JWT_SECRET` with a strong random secret and avoid committing real API keys.

## API Overview

All backend routes are under:

```text
/api/v1
```

Main route groups:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`
- `POST /resumes`
- `GET /resumes`
- `POST /job-descriptions/text`
- `POST /job-descriptions/pdf`
- `POST /reports`
- `GET /reports`
- `GET /reports/{id}`
- `POST /reports/{id}/retry`
- `POST /chat/sessions`
- `GET /chat/sessions`
- `POST /chat/sessions/{id}/messages`
- `GET /dashboard/summary`

## Build Checks

Backend:

```powershell
cd backend
mvn test
```

Frontend:

```powershell
cd frontend
npm install
npm run build
```

## Redis Status

Redis is currently provisioned in Docker Compose and the backend container depends on it. The backend does not currently use Redis through `RedisTemplate`, Spring Cache, or Spring Data Redis.

Accurate wording:

```text
Docker Compose provisions Redis for local infrastructure and future caching/rate-limiting support.
```

Do not claim Redis-backed caching or rate limiting unless that feature is implemented.

## Resume-Safe Project Summary

```text
InterviewIQ | Spring Boot, Spring Security, Spring AI, Ollama, React
- Architected an AI-powered interview preparation platform using a self-hosted Llama 3.1 model via Ollama, integrated through Spring AI and abstracted behind a provider-agnostic AiClient layer to generate personalized resume-to-job-description comparison reports.
- Established a secure authentication layer using HMAC-signed JWT access tokens and HttpOnly refresh token rotation, enforcing repository-level ownership scoping via Spring Security and Spring Data JPA.
- Built an asynchronous report generation pipeline with automatic retry/repair for malformed LLM JSON responses, backed by a normalized MySQL schema managed through Flyway migrations.
- Constructed a context-aware AI chat feature with an 8-message sliding-window conversational memory and a React/Redux Toolkit frontend supporting report status polling, containerized via Docker Compose.
```

## Notes

- Access tokens are HMAC-signed JWTs, not RS256.
- Refresh token rotation is implemented by revoking the used refresh token and issuing a replacement token.
- The provider-agnostic AI layer is the application-level `AiClient` interface.
- The Ollama implementation uses Spring AI `ChatClient`.
- Report status updates are implemented with frontend polling, not WebSockets or Server-Sent Events.
