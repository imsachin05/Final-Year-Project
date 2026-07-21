import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api'
import Modal from '../components/Modal'

const money = (v) => {
  if (v === null || v === undefined) return '—'
  const num = Number(v)
  const sign = num > 0 ? '+' : ''
  return `${sign}$${num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export default function Trades() {
  const [trades, setTrades] = useState([])
  const [loading, setLoading] = useState(true)
  const [aiModal, setAiModal] = useState(null) // { title, text, loading, error }
  const navigate = useNavigate()

  function loadTrades() {
    api.get('/trades').then((res) => {
      setTrades(res.data)
      setLoading(false)
    })
  }

  useEffect(() => { loadTrades() }, [])

  async function handleDelete(id) {
    if (!window.confirm('Delete this trade? This cannot be undone.')) return
    await api.delete(`/trades/${id}`)
    loadTrades()
  }

  async function openAiModal(title, endpoint) {
    setAiModal({ title, text: '', loading: true, error: '' })
    try {
      const res = await api.post(endpoint)
      setAiModal({ title, text: res.data.message, loading: false, error: '' })
    } catch (err) {
      setAiModal({ title, text: '', loading: false, error: err.response?.data?.message || 'Something went wrong.' })
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Trades</h1>
          <div className="page-subtitle">{trades.length} trade{trades.length !== 1 ? 's' : ''} logged</div>
        </div>
        <Link to="/trades/new" className="btn btn-primary">+ Add Trade</Link>
      </div>

      {loading ? (
        <p style={{ color: 'var(--text-muted)' }}>Loading…</p>
      ) : trades.length === 0 ? (
        <div className="card empty-state">
          <h3>No trades yet</h3>
          <p>Log your first trade to start seeing your stats.</p>
          <Link to="/trades/new" className="btn btn-primary" style={{ display: 'inline-block', marginTop: 12 }}>
            + Add Trade
          </Link>
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Symbol</th>
                <th>Type</th>
                <th>Status</th>
                <th>Entry</th>
                <th>Exit</th>
                <th>Qty</th>
                <th>R:R</th>
                <th>P/L</th>
                <th>Date</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {trades.map((t) => (
                <tr key={t.id}>
                  <td style={{ fontWeight: 600 }}>{t.symbol}</td>
                  <td><span className={`badge ${t.tradeType === 'LONG' ? 'badge-long' : 'badge-short'}`}>{t.tradeType}</span></td>
                  <td><span className={`badge ${t.status === 'OPEN' ? 'badge-open' : 'badge-closed'}`}>{t.status}</span></td>
                  <td className="mono">${Number(t.entryPrice).toFixed(2)}</td>
                  <td className="mono">{t.exitPrice ? `$${Number(t.exitPrice).toFixed(2)}` : '—'}</td>
                  <td className="mono">{t.quantity}</td>
                  <td className="mono">{t.riskRewardRatio ? `1:${t.riskRewardRatio}` : '—'}</td>
                  <td className={`mono ${t.pnl > 0 ? 'gain' : t.pnl < 0 ? 'loss' : ''}`}>{money(t.pnl)}</td>
                  <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>
                    {new Date(t.entryDate).toLocaleDateString()}
                  </td>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    <button className="icon-btn" onClick={() => openAiModal(`AI Trade Coach — ${t.symbol}`, `/ai/trade-feedback/${t.id}`)}>Coach</button>
                    <button className="icon-btn" onClick={() => openAiModal(`AI Trade Rating — ${t.symbol}`, `/ai/trade-rating/${t.id}`)}>Rate</button>
                    <button className="icon-btn" onClick={() => navigate(`/trades/${t.id}/edit`)}>Edit</button>
                    <button className="icon-btn" style={{ color: 'var(--loss)' }} onClick={() => handleDelete(t.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {aiModal && (
        <Modal title={aiModal.title} onClose={() => setAiModal(null)}>
          {aiModal.loading && <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>Thinking…</p>}
          {aiModal.error && <div className="error-banner">{aiModal.error}</div>}
          {aiModal.text && (
            <div style={{ fontSize: 13.5, lineHeight: 1.6, whiteSpace: 'pre-wrap' }}>{aiModal.text}</div>
          )}
        </Modal>
      )}
    </div>
  )
}
