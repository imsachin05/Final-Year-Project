import { useEffect, useState } from 'react'
import api from '../api'
import AiActionCard from './AiActionCard'

export default function StrategyAnalyzerCard() {
  const [stats, setStats] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/analytics/by-strategy').then((res) => {
      setStats(res.data.stats)
      setLoading(false)
    })
  }, [])

  return (
    <div className="card">
      <div style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 600, marginBottom: 4 }}>
        AI STRATEGY ANALYZER
      </div>
      <div style={{ fontSize: 12, color: 'var(--text-faint)', marginBottom: 14 }}>
        Performance broken down by strategy tag
      </div>

      {loading ? (
        <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>Loading…</p>
      ) : stats.length === 0 ? (
        <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>
          No closed trades tagged with a strategy yet.
        </p>
      ) : (
        <div style={{ overflowX: 'auto', marginBottom: 16 }}>
          <table>
            <thead>
              <tr>
                <th>Strategy</th>
                <th>Trades</th>
                <th>Win Rate</th>
                <th>Total P/L</th>
                <th>Avg P/L</th>
                <th>Avg R:R</th>
              </tr>
            </thead>
            <tbody>
              {stats.map((s) => (
                <tr key={s.strategy}>
                  <td style={{ fontWeight: 600 }}>{s.strategy}</td>
                  <td className="mono">{s.totalTrades}</td>
                  <td className="mono">{s.winRatePercent}%</td>
                  <td className={`mono ${s.totalPnl > 0 ? 'gain' : s.totalPnl < 0 ? 'loss' : ''}`}>
                    ${Number(s.totalPnl).toFixed(2)}
                  </td>
                  <td className="mono">${Number(s.averagePnl).toFixed(2)}</td>
                  <td className="mono">1:{s.averageRiskRewardRatio}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div style={{ borderTop: '1px solid var(--border)', paddingTop: 14 }}>
        <AiActionCard bare endpoint="/ai/strategy-analysis" buttonLabel="Get AI Take" />
      </div>
    </div>
  )
}
