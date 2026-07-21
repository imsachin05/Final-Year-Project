import { useState } from 'react'
import api from '../api'

function formatText(text) {
  return text.split('\n').map((line, i) => (
    <p key={i} style={{ margin: line.trim() === '' ? '10px 0' : '4px 0' }}>{line}</p>
  ))
}

/**
 * Generic "click to generate" AI card. Used for every AI feature that's a single
 * on-demand text generation: Weekly Report, Journal Summary, Emotion Detection,
 * Strategy Analyzer narrative, etc. Fetches nothing until the user asks for it,
 * so opening the AI Dashboard doesn't fire five AI calls at once.
 */
export default function AiActionCard({ title, subtitle, endpoint, method = 'get', buttonLabel = 'Generate', bare = false }) {
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function generate() {
    setLoading(true)
    setError('')
    try {
      const res = method === 'post' ? await api.post(endpoint) : await api.get(endpoint)
      setResult(res.data.message)
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong generating this.')
    } finally {
      setLoading(false)
    }
  }

  const content = (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: result || error ? 14 : 0 }}>
        <div>
          {title && <div style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 600 }}>{title}</div>}
          {subtitle && <div style={{ fontSize: 12, color: 'var(--text-faint)', marginTop: 2 }}>{subtitle}</div>}
        </div>
        <button className="btn btn-secondary" onClick={generate} disabled={loading}>
          {loading ? 'Working…' : result ? 'Regenerate' : buttonLabel}
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {result && (
        <div style={{ fontSize: 13.5, lineHeight: 1.6 }}>
          {formatText(result)}
        </div>
      )}
    </>
  )

  return bare ? content : <div className="card">{content}</div>
}
