import { useState } from 'react'
import api from '../api'

function formatInsight(text) {
  // Light formatting: turn "1. " / "- " style lines into a cleaner block without
  // pulling in a full markdown renderer just for this.
  return text.split('\n').map((line, i) => (
    <p key={i} style={{ margin: line.trim() === '' ? '10px 0' : '4px 0' }}>{line}</p>
  ))
}

export default function AiInsightsCard() {
  const [insight, setInsight] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function generate() {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/ai/insights')
      setInsight(res.data.message)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not generate insights right now.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card" style={{ marginBottom: 20 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: insight || error ? 14 : 0 }}>
        <div>
          <div style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 600 }}>AI INSIGHTS</div>
          <div style={{ fontSize: 12, color: 'var(--text-faint)', marginTop: 2 }}>
            Patterns and coaching feedback based on your trade history
          </div>
        </div>
        <button className="btn btn-secondary" onClick={generate} disabled={loading}>
          {loading ? 'Analyzing…' : insight ? 'Regenerate' : 'Generate Insights'}
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {insight && (
        <div style={{ fontSize: 13.5, lineHeight: 1.6, color: 'var(--text-primary)' }}>
          {formatInsight(insight)}
        </div>
      )}
    </div>
  )
}
