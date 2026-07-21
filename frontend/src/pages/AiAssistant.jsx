import { useRef, useState } from 'react'
import api from '../api'

const SUGGESTIONS = [
  'Which strategy is working best for me?',
  'Do I perform worse on short trades than long trades?',
  'Am I respecting my stop losses?',
  "What's the biggest risk pattern I should fix?",
]

export default function AiAssistant() {
  const [messages, setMessages] = useState([
    { role: 'assistant', content: "Ask me anything about your trading history — win rate by strategy, risk discipline, patterns in your notes, whatever you're curious about." }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const bottomRef = useRef(null)

  async function send(question) {
    const q = question ?? input
    if (!q.trim() || loading) return

    setMessages((m) => [...m, { role: 'user', content: q }])
    setInput('')
    setLoading(true)
    setError('')

    try {
      const res = await api.post('/ai/ask', { question: q })
      setMessages((m) => [...m, { role: 'assistant', content: res.data.message }])
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reach the AI assistant right now.')
    } finally {
      setLoading(false)
      setTimeout(() => bottomRef.current?.scrollIntoView({ behavior: 'smooth' }), 50)
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 56px)' }}>
      <div className="page-header">
        <div>
          <h1>Ask Your Journal</h1>
          <div className="page-subtitle">An AI assistant grounded in your own recorded trades</div>
        </div>
      </div>

      <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div style={{ flex: 1, overflowY: 'auto', paddingRight: 4 }}>
          {messages.map((m, i) => (
            <div
              key={i}
              style={{
                display: 'flex',
                justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start',
                marginBottom: 12,
              }}
            >
              <div style={{
                maxWidth: '78%',
                background: m.role === 'user' ? 'var(--accent-dim)' : 'var(--surface-alt)',
                border: `1px solid ${m.role === 'user' ? 'var(--accent)' : 'var(--border)'}`,
                borderRadius: 10,
                padding: '10px 14px',
                fontSize: 13.5,
                lineHeight: 1.6,
                whiteSpace: 'pre-wrap',
              }}>
                {m.content}
              </div>
            </div>
          ))}
          {loading && (
            <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>Thinking…</div>
          )}
          {error && <div className="error-banner">{error}</div>}
          <div ref={bottomRef} />
        </div>

        {messages.length <= 1 && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 12 }}>
            {SUGGESTIONS.map((s) => (
              <button key={s} className="btn btn-secondary" style={{ fontSize: 12, padding: '6px 12px' }} onClick={() => send(s)}>
                {s}
              </button>
            ))}
          </div>
        )}

        <form
          onSubmit={(e) => { e.preventDefault(); send() }}
          style={{ display: 'flex', gap: 10, marginTop: 4 }}
        >
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask a question about your trading history…"
            style={{
              flex: 1, background: 'var(--surface-alt)', border: '1px solid var(--border)',
              borderRadius: 8, padding: '10px 14px', color: 'var(--text-primary)', fontSize: 14, outline: 'none',
            }}
          />
          <button className="btn btn-primary" disabled={loading}>Send</button>
        </form>
      </div>
    </div>
  )
}
