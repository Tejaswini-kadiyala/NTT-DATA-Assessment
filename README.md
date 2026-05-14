# Full-Stack Code Refactor Assistant

This project is a full-stack application with a React.js frontend and a Spring Boot backend. It analyzes code, detects issues, and provides AI-powered refactoring suggestions.

## Project Structure

- `frontend/` — React.js app (code input, results display)
- `backend/` — Spring Boot REST API (code analysis, AI integration)

## Features

### Frontend (React.js)
- Textarea for code input
- Submit button
- Sections for:
  - Original Code
  - Refactored Code
  - Explanation
  - Warnings

### Backend (Spring Boot)
- REST API endpoint `/api/refactor`
- Detects long methods and duplicate lines
- Calls AI API (OpenAI/Hugging Face) for refactoring (placeholder)
- Returns formatted response

## Getting Started

### Frontend
1. `cd frontend`
2. `npm install`
3. `npm start`

### Backend
1. `cd backend`
2. `mvn spring-boot:run`

Backend runs on: `http://localhost:8080`

---

## Technologies Used
- React.js
- Spring Boot
- REST API
- Axios
- Maven

---

## Notes
- Replace AI API placeholders with your actual API key and logic.
- For more features, see project documentation.
