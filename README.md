# Final Year Project — Trading Journal & Performance Analytics System

A full-stack web app for traders to log trades and automatically get win rate, risk-reward,
profit factor, expectancy, drawdown, and equity-curve analytics.

**Stack:** Spring Boot (Java 17) + Spring Security (JWT) + H2 (embedded, file-based) on the
backend; React (Vite) + Recharts on the frontend.

---

## 1. Prerequisites

- **Java 17+** and **Maven** (or an IDE with Maven built in, e.g. IntelliJ / Eclipse / VS Code + Java extensions)
- **Node.js 18+** and npm

Check versions:
```bash
java -version
mvn -version
node -version
```

No MySQL/Postgres install needed — the backend uses an embedded H2 database that saves to a
local file (`backend/data/finalyearproject.mv.db`), created automatically on first run.

---

## 2. Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

On first startup, a **demo account is auto-created** so your dashboard isn't empty:

- Username: `demo`
- Password: `demo1234`

It comes preloaded with ~40 realistic sample trades across 75 days, so charts, win rate,
equity curve, and drawdown all have real data to show immediately — useful for screenshots,
your report, and your viva. To turn this off later (e.g. for a clean submission), set
`app.demo-data.enabled=false` in `src/main/resources/application.properties` and delete the
`backend/data/` folder to reset the database.

You can also register your own account from the frontend at any time — it uses the same
`/api/auth/register` endpoint the demo data does.

**H2 console** (to inspect the database directly): http://localhost:8080/h2-console
JDBC URL: `jdbc:h2:file:./data/finalyearproject`, username `sa`, no password.

---

## 3. Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. The Vite dev server proxies `/api/*` requests to the backend
on port 8080 (see `vite.config.js`), so there's no CORS friction during development.

---

## 4. What's implemented

- **Auth**: register/login with JWT, passwords hashed with BCrypt
- **Trade CRUD**: create, edit, delete trades (symbol, long/short, entry/exit price, quantity,
  stop-loss, take-profit, strategy tag, notes, open/closed status)
- **Auto-calculated per trade**: P/L, risk-reward ratio, win/loss
- **Dashboard analytics** (`GET /api/analytics`): total P/L, win rate, profit factor,
  expectancy per trade, average risk-reward, max drawdown, current win/loss streak,
  equity curve, monthly P/L breakdown
- **Charts**: equity curve (area chart), monthly P/L (bar chart), win/loss split (donut)

## 5. AI Features

All ten live under **AI Dashboard** (`/ai`) and **Ask Your Journal** (`/assistant`) in the
frontend, backed by `AiController` / `AiCoachService` on the backend. They all analyze the
trader's **own recorded trade history** — none of them predict live markets or recommend
buying/selling anything, which is a deliberate scope decision explained more below.

| # | Feature | Where | What it does |
|---|---|---|---|
| 1 | **AI Trade Coach** | Trades table → "Coach" button | Per-trade written feedback: was risk managed well, does the outcome match the plan, one thing to reflect on |
| 2 | **AI Trade Rating** | Trades table → "Rate" button | Grades a single trade A-F on *execution quality* (risk management), not on whether it made money |
| 3 | **AI Emotion Detection** | AI Dashboard | Scans your own written trade notes for trading-psychology patterns — FOMO, revenge trading, hesitation, overconfidence — quoting the notes that show it |
| 4 | **AI Weekly Report** | AI Dashboard | Coach-style check-in narrative on your last 7 days of closed trades |
| 5 | **AI Journal Summary** | AI Dashboard | Summarizes recurring *themes* across all your notes (separate from the stats-based Insights card) |
| 6 | **AI Strategy Analyzer** | AI Dashboard | Deterministic win-rate/P&L table grouped by strategy tag, plus an AI narrative on which strategy is actually working |
| 7 | **AI Risk Calculator** | AI Dashboard | Position-size calculator (account balance, risk %, entry, stop loss → suggested quantity) — pure arithmetic, works even without an AI key — plus an optional AI commentary on the setup |
| 8 | **AI Trade Outlook** | AI Dashboard | Look up a symbol + strategy combo and see how *your own* past trades with that setup performed. Explicitly framed as history, not a forecast |
| 9 | **AI Chat Assistant** | Ask Your Journal page | Free-text Q&A grounded in your trade data ("Which strategy is working best for me?") |
| 10 | **AI Dashboard** | `/ai` | The hub page combining all of the above |

There's also an **AI Insights** card on the main Dashboard — a bonus 11th capability giving an
overall strengths/weaknesses/suggestions readout, separate from the more focused features above.

### Why "Trade Outlook" instead of "Trade Prediction"

An AI model cannot actually predict future price movement, and a feature that implied it could
would be misleading — worth avoiding in a real project, and worth mentioning in your report as
a deliberate design decision. Trade Outlook keeps the useful part (pattern recognition on your
own history) and drops the part that would overpromise: it always frames results as "this is
what happened when you did this before," with an explicit small-sample-size warning under 5
matching trades, never as "this is what will happen."

### Setting up your AI API key

The AI features need an API key from an LLM provider. In
`backend/src/main/resources/application.properties`:

```properties
app.ai.api-key=sk-your-key-here
app.ai.base-url=https://api.openai.com/v1
app.ai.model=gpt-4o-mini
```

This works with OpenAI directly, or any other provider that exposes an OpenAI-compatible
`/chat/completions` endpoint (Groq, OpenRouter, a local Ollama server with its OpenAI-compatible
API enabled, etc.) — just change `base-url` and `model`. Without a key configured, every AI
endpoint returns a clear error message telling you to set one; the rest of the app (trade CRUD,
analytics, charts) works completely independently of this.

## 6. Deliberately cut for the 2-day timeline (call these out as "future work" in your report)

- CSV import from broker exports (Zerodha/Binance/MT4)
- PDF/Excel report export
- Trading psychology/emotion tagging
- Strategy-vs-strategy comparison view

These are good "Future Enhancements" bullet points for your project report — they show you
scoped deliberately rather than ran out of ideas.

---

## 7. Project structure

```
final-year-project/
├── backend/
│   └── src/main/java/com/tradingjournal/
│       ├── model/          Trade, User, enums
│       ├── repository/     Spring Data JPA repositories
│       ├── dto/             Request/response objects (trades, analytics, AI, risk calc)
│       ├── service/         TradeService, AnalyticsService, AuthService, RiskCalculatorService
│       ├── ai/               AiClient (LLM HTTP call) + AiCoachService (prompt building for all 10 AI features)
│       ├── controller/      REST endpoints, incl. AiController for every /api/ai/* route
│       ├── security/        JWT filter + util
│       ├── config/          Security config, CORS, demo data seeder
│       └── exception/       Centralized error handling
└── frontend/
    └── src/
        ├── pages/            Login, Register, Dashboard, Trades, AddTrade, AiDashboard, AiAssistant
        ├── components/       Sidebar, StatCard, chart components, Modal, AiInsightsCard,
        │                     AiActionCard, StrategyAnalyzerCard, RiskCalculatorCard, TradeOutlookCard
        ├── context/          AuthContext (JWT storage + login/register/logout)
        └── styles/           Design tokens + global CSS
```

## 8. Switching from H2 to MySQL later (optional, if your supervisor expects it)

Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

Replace the datasource block in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/winning_zone?createDatabaseIfNotExist=true
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

Everything else (entities, repositories, services) stays exactly the same — that's one of the
selling points of using JPA/Hibernate you can mention in your report.

## 9. For your report/viva

Good talking points this project already gives you:
- **Layered architecture** (controller → service → repository) — standard enterprise Java pattern
- **Stateless JWT auth** — explain why (scalability, no server-side session storage)
- **Derived vs. stored data** — P/L and risk-reward are *calculated on read*, not stored, so
  they're always consistent with the underlying trade data (good database design talking point)
- **Equity curve & drawdown** — shows you understand basic risk/performance metrics used in
  real trading, not just a CRUD app with charts bolted on
- **AI scope discipline** — every AI feature analyzes the user's OWN historical data rather
  than predicting markets or giving buy/sell advice; a good example of thinking through the
  ethics and limits of an AI feature rather than bolting one on for its own sake
- **Provider-agnostic AI client** — `AiClient` talks to any OpenAI-compatible endpoint, so
  swapping providers (OpenAI, Groq, a local model) is a config change, not a code change
