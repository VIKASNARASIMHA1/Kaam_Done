# Kaam Done — Full-Stack Project & Task Management System

A Trello/Jira-style task management app built to demonstrate a complete full-stack skill set:
**Java 17 + Spring Boot 3 (REST API, Spring Security, JWT auth, Spring Data JPA)** on the backend,
and **React 18 + Vite (Hooks, Context API, React Router, Axios)** on the frontend.

## Features

- JWT-based authentication (register / login), passwords hashed with BCrypt
- **Team projects with roles** — invite teammates as Editor or Viewer; the project owner manages membership and permissions
- Full CRUD on Projects and Tasks, with role-aware permissions (Viewers get read-only access everywhere)
- **Drag-and-drop Kanban board** (To Do / In Progress / Done) built with `@dnd-kit`
- **Comments on tasks** — threaded discussion per task, with delete rights for the author or project owner
- **Activity feed / audit log** — every project/task/member change is recorded and viewable per project
- **File attachments** — upload/download files per task, stored on disk, access-controlled like everything else
- **Dashboard analytics** — a stacked bar chart (done vs remaining tasks per project) and a completion donut chart, built with Recharts
- **Dark mode toggle** — persisted per-browser
- Task priority (Low/Medium/High) and due dates
- Global exception handling with clean JSON error responses (404 / 400 / 401 / 403 / 500)
- Layered architecture: Controller → Service → Repository → Entity, with DTOs, plus a dedicated `ProjectAccessService` centralizing all role/permission checks
- H2 file-based database — zero setup, no external DB server required
- CORS configured for local frontend/backend split

## Tech Stack

| Layer      | Technology                                                   |
|------------|---------------------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, H2, JWT (jjwt), Maven, Lombok |
| Frontend   | React 18, Vite, React Router 6, Axios, @dnd-kit (drag-and-drop), Recharts (charts), plain CSS with light/dark theming |

## Project Structure

```
taskflow/
├── backend/                     Spring Boot REST API (port 8080)
│   ├── pom.xml
│   └── src/main/java/com/taskflow/
│       ├── config/              Security & CORS config
│       ├── security/            JWT util, filter, entry point
│       ├── controller/          REST controllers
│       ├── service/             Business logic
│       ├── repository/          Spring Data JPA repositories
│       ├── entity/               JPA entities (User, Project, Task)
│       ├── dto/                 Request/response DTOs
│       └── exception/           Global exception handling
├── frontend/                    React + Vite SPA (port 5173)
│   └── src/
│       ├── api/                 Axios instance with JWT interceptor
│       ├── context/             Auth context + Theme (dark mode) context
│       ├── components/          Navbar, ProjectCard, TaskCard (draggable), KanbanColumn,
│       │                        TaskModal (create), TaskDetailModal (edit/comments/attachments),
│       │                        MembersPanel, ActivityFeed, DashboardAnalytics, RoleBadge
│       └── pages/                Login, Register, Dashboard, ProjectDetail (Board/Activity/Members tabs)
└── README.md
```

---

## Prerequisites

You said you already have these installed — verify versions in PowerShell:

```powershell
java -version      # Should be 17 or higher
mvn -version        # Any recent Maven 3.8+
node -version        # Should be 18 or higher
npm -version
```

---

## Running the project on Windows PowerShell

### 1. Unzip and open two PowerShell windows

Unzip the project, e.g. to `C:\Projects\taskflow`. You'll run the backend and frontend in **two separate PowerShell windows** since both need to stay running.

### 2. Start the backend (Spring Boot API)

In the **first** PowerShell window:

```powershell
cd C:\Projects\taskflow\backend
mvn clean install
mvn spring-boot:run
```

- First run will download dependencies (takes a minute or two).
- The API starts on **http://localhost:8080**
- It auto-creates a local H2 database file at `backend/data/taskflow.mv.db` — no DB installation needed.
- You'll know it's ready when you see: `Started TaskflowApplication in ... seconds`
- (Optional) Inspect the database at http://localhost:8080/h2-console — JDBC URL: `jdbc:h2:file:./data/taskflow`, user `sa`, blank password.

Leave this window running.

### 3. Start the frontend (React)

In the **second** PowerShell window:

```powershell
cd C:\Projects\taskflow\frontend
npm install
npm run dev
```

- This starts the Vite dev server on **http://localhost:5173**
- Vite is pre-configured to proxy any `/api/**` request to `http://localhost:8080`, so the frontend and backend talk to each other with no extra config.

### 4. Open the app

Go to **http://localhost:5173** in your browser:

1. Click **Sign up** to create an account.
2. You'll be logged in automatically and land on the Dashboard.
3. Create a Project, click into it, and add Tasks — **drag task cards between columns** to change status, or click a card to open its full detail view (edit fields, comments, attachments).
4. Open the **Members** tab on a project to invite teammates by username as an Editor or Viewer. Only the project owner can manage members.
5. Open the **Activity** tab to see a live audit trail of everything that happened on the project.
6. Toggle dark mode from the moon/sun icon in the navbar.

---

## Stopping the app

In each PowerShell window, press `Ctrl + C` to stop the process.

## Resetting the database

To start fresh, stop the backend and delete the `backend/data` folder (and optionally `backend/uploads` to also clear uploaded files):

```powershell
cd C:\Projects\taskflow\backend
Remove-Item -Recurse -Force data
Remove-Item -Recurse -Force uploads -ErrorAction SilentlyContinue
```

---

## API Overview

| Method | Endpoint                                                        | Auth required | Description                          |
|--------|-------------------------------------------------------------------|----------------|---------------------------------------|
| POST   | `/api/auth/register`                                               | No             | Create account, returns JWT            |
| POST   | `/api/auth/login`                                                  | No             | Login, returns JWT                     |
| GET    | `/api/users/me`                                                    | Yes            | Current user profile                   |
| GET    | `/api/projects`                                                    | Yes            | List projects you own or are a member of |
| POST   | `/api/projects`                                                    | Yes            | Create a project                       |
| GET    | `/api/projects/{id}`                                               | Yes            | Get one project                        |
| PUT    | `/api/projects/{id}`                                               | Yes (Editor+)  | Update a project                       |
| DELETE | `/api/projects/{id}`                                               | Yes (Owner)    | Delete a project (+ tasks)             |
| GET    | `/api/projects/{id}/members`                                       | Yes            | List members (+ owner)                 |
| POST   | `/api/projects/{id}/members`                                       | Yes (Owner)    | Add a member by username, with a role   |
| PUT    | `/api/projects/{id}/members/{memberId}`                            | Yes (Owner)    | Change a member's role                 |
| DELETE | `/api/projects/{id}/members/{memberId}`                            | Yes (Owner)    | Remove a member                        |
| GET    | `/api/projects/{id}/activity`                                      | Yes            | Recent activity log for the project     |
| GET    | `/api/projects/{id}/tasks`                                         | Yes            | List tasks in a project                |
| POST   | `/api/projects/{id}/tasks`                                         | Yes (Editor+)  | Create a task                          |
| PUT    | `/api/projects/{id}/tasks/{taskId}`                                | Yes (Editor+)  | Update a task                          |
| DELETE | `/api/projects/{id}/tasks/{taskId}`                                | Yes (Editor+)  | Delete a task                          |
| GET    | `/api/projects/{id}/tasks/{taskId}/comments`                       | Yes            | List comments on a task                |
| POST   | `/api/projects/{id}/tasks/{taskId}/comments`                       | Yes (Editor+)  | Add a comment                          |
| DELETE | `/api/projects/{id}/tasks/{taskId}/comments/{commentId}`           | Yes (author/Owner) | Delete a comment                    |
| GET    | `/api/projects/{id}/tasks/{taskId}/attachments`                    | Yes            | List a task's attachments              |
| POST   | `/api/projects/{id}/tasks/{taskId}/attachments`                    | Yes (Editor+)  | Upload a file (multipart/form-data)     |
| GET    | `/api/projects/{id}/tasks/{taskId}/attachments/{attId}/download`   | Yes            | Download a file                        |
| DELETE | `/api/projects/{id}/tasks/{taskId}/attachments/{attId}`            | Yes (Editor+)  | Delete an attachment                   |

All authenticated requests need header: `Authorization: Bearer <token>`.

### Roles

- **Owner** — created the project; full control including managing members and deleting the project.
- **Editor** — can create/update/delete tasks, comment, and upload attachments, but cannot manage members or delete the project.
- **Viewer** — read-only access to everything (board, comments, attachments, activity).

## Why this is a good resume project

- Demonstrates a realistic layered backend architecture (not a toy CRUD single-file demo)
- Real authentication/authorization, not just an open API
- Role-based access control across a shared/collaborative data model — a very common interview topic, going beyond simple per-user ownership
- An audit-log pattern (who did what, when) — frequently asked about in backend interviews
- File upload/download handling with access control
- Clean separation of concerns between frontend state (Context), API layer (Axios), and UI
- Real drag-and-drop UX and data visualization, not just static CRUD screens

## Suggested next steps for extending it

- Swap H2 for PostgreSQL/MySQL for a production deployment
- Add pagination and search/filtering on tasks
- Add role-based admin views (the `Role` enum already supports `ROLE_ADMIN`)
- Add unit/integration tests (JUnit 5 + Mockito for backend, Vitest + React Testing Library for frontend)
- Move file storage to S3-compatible object storage instead of local disk
- Add WebSocket live updates so the board refreshes in real time for all collaborators
- Deploy: backend to Render/Railway/Fly.io, frontend to Vercel/Netlify, and point the Vite proxy / an env variable at the deployed API URL
