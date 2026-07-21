import { useEffect, useState } from 'react'
import api from '../api'
import StatCard from '../components/StatCard'
import EquityCurveChart from '../components/EquityCurveChart'
import MonthlyPnlChart from '../components/MonthlyPnlChart'
import WinLossDonut from '../components/WinLossDonut'
import AiInsightsCard from '../components/AiInsightsCard'

const money = (v) => {
  const num = Number(v)
  const sign = num > 0 ? '+' : ''
  return `${sign}$${num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export default function Dashboard() {
  const [analytics, setAnalytics] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/analytics').then((res) => {
      setAnalytics(res.data)
      setLoading(false)
    })
  }, [])

  if (loading || !analytics) {
    return (
      <div>
        <div className="page-header"><h1>Dashboard</h1></div>
        <p style={{ color: 'var(--text-muted)' }}>Loading analytics…</p>
      </div>
    )
  }

  const pnlTone = Number(analytics.totalPnl) > 0 ? 'gain' : Number(analytics.totalPnl) < 0 ? 'loss' : null

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <div className="page-subtitle">
            {analytics.totalTrades} total trades · {analytics.closedTrades} closed
          </div>
        </div>
      </div>

      <div className="stat-grid">
        <StatCard label="Total P/L" value={money(analytics.totalPnl)} tone={pnlTone} />
        <StatCard label="Win Rate" value={`${analytics.winRatePercent}%`} />
        <StatCard label="Profit Factor" value={analytics.profitFactor >= 999 ? '∞' : analytics.profitFactor} />
        <StatCard label="Expectancy / Trade" value={money(analytics.expectancy)} tone={analytics.expectancy >= 0 ? 'gain' : 'loss'} />
        <StatCard label="Avg Risk:Reward" value={`1 : ${analytics.averageRiskRewardRatio}`} />
        <StatCard label="Max Drawdown" value={`-$${Number(analytics.maxDrawdown).toLocaleString(undefined, { minimumFractionDigits: 2 })}`} tone="loss" />
        <StatCard label="Avg Win" value={money(analytics.averageWin)} tone="gain" />
        <StatCard label="Avg Loss" value={`-${money(analytics.averageLoss).replace('+', '')}`} tone="loss" />
      </div>

      <AiInsightsCard />

      <div className="chart-row">
        <EquityCurveChart data={analytics.equityCurve} />
        <WinLossDonut wins={analytics.winningTrades} losses={analytics.losingTrades} />
      </div>

      <MonthlyPnlChart data={analytics.monthlyPnl} />
    </div>
  )
}
