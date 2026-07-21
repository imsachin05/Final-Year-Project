import { useState } from 'react'
import api from '../api'

export default function TradeOutlookCard() {
  const [symbol, setSymbol] = useState('')
  const [strategy, setStrategy] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function lookup(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/ai/trade-outlook', { params: { symbol, strategy } })
      setResult(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not look up your history for this setup.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <div style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 600, marginBottom: 4 }}>
        AI TRADE OUTLOOK
      </div>
      <div style={{ fontSize: 12, color: 'var(--text-faint)', marginBottom: 14 }}>
        How YOUR past trades with this setup performed — a look backward, not a market forecast
      </div>

      <form onSubmit={lookup} style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
        <input
          value={symbol}
          onChange={(e) => setSymbol(e.target.value)}
          placeholder="Symbol (e.g. AAPL)"
          style={{ flex: 1, minWidth: 140, background: 'var(--surface-alt)', border: '1px solid var(--border)', borderRadius: 8, padding: '10px 12px', color: 'var(--text-primary)', fontSize: 13 }}
        />
        <input
          value={strategy}
          onChange={(e) => setStrategy(e.target.value)}
          placeholder="Strategy (e.g. Breakout)"
          style={{ flex: 1, minWidth: 140, background: 'var(--surface-alt)', border: '1px solid var(--border)', borderRadius: 8, padding: '10px 12px', color: 'var(--text-primary)', fontSize: 13 }}
        />
        <button className="btn btn-primary" disabled={loading}>{loading ? 'Looking…' : 'Look Up'}</button>
      </form>

      {error && <div className="error-banner" style={{ marginTop: 14 }}>{error}</div>}

      {result && (
        <div style={{ marginTop: 16 }}>
          {result.matchingTradeCount === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              No past trades match that setup yet.
            </p>
          ) : (
            <>
              <div className="stat-grid" style={{ marginBottom: 12 }}>
                <div className="stat-card">
                  <div className="stat-label">Matching Trades</div>
                  <div className="stat-value mono">{result.matchingTradeCount}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Win Rate</div>
                  <div className="stat-value mono">{result.winRatePercent}%</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Avg P/L</div>
                  <div className={`stat-value mono ${result.averagePnl > 0 ? 'gain' : result.averagePnl < 0 ? 'loss' : ''}`}>
                    ${Number(result.averagePnl).toFixed(2)}
                  </div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Avg R:R</div>
                  <div className="stat-value mono">1:{result.averageRiskRewardRatio}</div>
                </div>
              </div>
              <div style={{ fontSize: 13.5, lineHeight: 1.6, color: 'var(--text-primary)' }}>{result.narrative}</div>
            </>
          )}
        </div>
      )}
    </div>
  )
}
