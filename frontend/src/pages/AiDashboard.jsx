import { Link } from 'react-router-dom'
import AiInsightsCard from '../components/AiInsightsCard'
import AiActionCard from '../components/AiActionCard'
import StrategyAnalyzerCard from '../components/StrategyAnalyzerCard'
import RiskCalculatorCard from '../components/RiskCalculatorCard'
import TradeOutlookCard from '../components/TradeOutlookCard'

export default function AiDashboard() {
  return (
    <div>
      <div className="page-header">
        <div>
          <h1>AI Dashboard</h1>
          <div className="page-subtitle">
            Every AI feature in one place — all grounded in your own recorded trades, never live market advice
          </div>
        </div>
        <Link to="/assistant" className="btn btn-primary">Ask Your Journal →</Link>
      </div>

      <AiInsightsCard />

      <div className="chart-row" style={{ gridTemplateColumns: '1fr 1fr', marginBottom: 20 }}>
        <AiActionCard
          title="AI WEEKLY REPORT"
          subtitle="A coach's check-in on your last 7 days"
          endpoint="/ai/weekly-report"
          buttonLabel="Generate Report"
        />
        <AiActionCard
          title="AI JOURNAL SUMMARY"
          subtitle="Recurring themes across everything you've written"
          endpoint="/ai/journal-summary"
          buttonLabel="Summarize Journal"
        />
      </div>

      <div style={{ marginBottom: 20 }}>
        <AiActionCard
          title="AI EMOTION DETECTION"
          subtitle="Trading-psychology patterns visible in your notes (FOMO, revenge trading, discipline, etc.)"
          endpoint="/ai/emotion-analysis"
          buttonLabel="Analyze Notes"
        />
      </div>

      <div style={{ marginBottom: 20 }}>
        <StrategyAnalyzerCard />
      </div>

      <div className="chart-row" style={{ gridTemplateColumns: '1fr 1fr', marginBottom: 20 }}>
        <RiskCalculatorCard />
        <TradeOutlookCard />
      </div>

      <div className="card">
        <div style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 600, marginBottom: 6 }}>
          AI TRADE COACH &amp; AI TRADE RATING
        </div>
        <p style={{ fontSize: 13.5, color: 'var(--text-primary)', lineHeight: 1.6 }}>
          Per-trade coaching feedback and an A-F execution grade are available directly on each
          trade — open <Link to="/trades" style={{ color: 'var(--accent)', fontWeight: 600 }}>Trades</Link> and
          use the "Coach" and "Rate" buttons next to any closed trade.
        </p>
      </div>
    </div>
  )
}
