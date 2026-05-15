# Full-Stack Code Refactor Assistant

This repository contains a full-stack demo that analyzes and refactors code. It consists of a React frontend that sends code to a Spring Boot backend which calls an AI model (Gemini) to generate refactoring suggestions. A small deterministic fallback (`MiniService`) is included when the AI model does not return usable output.

## Project structure

- `frontend/` — React app (code input, results display)
- `backend/` — Spring Boot REST API (code analysis, AI integration)

## Key features

Frontend
- Textarea to paste code
- Button to request code analysis/refactor
- Sections for: Original Code, Refactored Code, Explanation, Warnings

Backend
- Endpoint: `POST /api/refactor` (accepts JSON: `{ "code": "..." }`)
- `GeminiService` — calls the Gemini generative API and attempts to parse the response into structured fields
- `MiniService` — deterministic fallback refactor (trims whitespace, removes duplicate blank lines) when the AI returns no useful result
- Returns a JSON `CodeResponse` with the fields: `originalCode`, `refactoredCode`, `explanation`, `warnings`

## Requirements

- Java 17 (project is configured with `<java.version>17</java.version>` in Maven)
- Maven
- Node.js and npm (for the React frontend)

## Configuration

Add your Gemini API key to the backend configuration.
Steps to how to get Gemini API Key
1. Get a Free API Key from Open Google AI Studio
2.Sign in with your Google Account
3.Click Get API Key
4.Create a new API Key
5.Copy the key (example key looks likes: ALZaSyXXXXXXXXXXXXXX)
You can either:
- Add it to `backend/src/main/resources/application.properties`:

```properties
gemini.api.key=YOUR_KEY_HERE
```

- Or set an environment variable before running the backend and update your properties accordingly.

Note: Keep API keys secret. In production, use a secrets manager or environment variables rather than committing them to source control.

## Running the app (development)

Start the backend:

```powershell
cd 'D:\Projects\New folder\backend'
mvn spring-boot:run
```

Or build and run the jar:

```powershell
cd 'D:\Projects\New folder\backend'
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

Start the frontend (in a separate terminal):

```powershell
cd 'D:\Projects\New folder\frontend'
npm install
npm start
```

- Frontend default: http://localhost:3000
- Backend default: http://localhost:8080

If port 3000 is in use, React will prompt to use another port — choose yes or free the port.

## Troubleshooting

- Frontend crash: "Cannot read properties of undefined (reading 'map')"
  - Cause: the frontend expected a structured JSON response with a `warnings` array but received undefined or a raw string.
  - Fixes included in this codebase:
    - `ResultSection` now guards against undefined values and handles raw string responses.
    - The backend returns a structured `CodeResponse`. If Gemini returns no useful output, the controller falls back to the `MiniService` deterministic refactor so the UI will display something meaningful.

- If you still see empty fields for "Refactored Code" or "Explanation":
  1. Ensure the backend is running and reachable at `http://localhost:8080`.
 2. Check backend logs for Gemini API errors (timeouts, 401/403 etc.). If the AI call fails, the fallback `MiniService` will be used.
 3. Confirm `gemini.api.key` is set correctly.

## Response format

The backend returns a JSON object shaped like this:

```json
{
  "originalCode": "...",
  "refactoredCode": "...",
  "explanation": "...",
  "warnings": ["..."]
}
```

## Development notes and next steps

- The AI response parsing is heuristic. If you have a sample Gemini API response JSON, add it to the backend and I can implement a precise parser to extract refactored code, naming suggestions, and explanation reliably.
- Consider adding timeouts and retries to `GeminiService`, and secure the API key with environment variables or a secrets manager.
- For richer fallback refactors, integrate language-specific formatters (Prettier for JS, google-java-format for Java, etc.) in `MiniService`.

## License & Contributing

This is a demo project — feel free to open issues or pull requests to improve parsing, error handling, and refactor quality.
