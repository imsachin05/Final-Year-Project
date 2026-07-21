import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api'

const emptyForm = {
  symbol: '',
  tradeType: 'LONG',
  status: 'OPEN',
  entryPrice: '',
  exitPrice: '',
  quantity: '',
  stopLoss: '',
  takeProfit: '',
  entryDate: '',
  exitDate: '',
  strategy: '',
  notes: '',
}

function toDatetimeLocal(iso) {
  if (!iso) return ''
  return iso.slice(0, 16)
}

export default function AddTrade() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (isEdit) {
      api.get(`/trades/${id}`).then((res) => {
        const t = res.data
        setForm({
          symbol: t.symbol,
          tradeType: t.tradeType,
          status: t.status,
          entryPrice: t.entryPrice,
          exitPrice: t.exitPrice ?? '',
          quantity: t.quantity,
          stopLoss: t.stopLoss ?? '',
          takeProfit: t.takeProfit ?? '',
          entryDate: toDatetimeLocal(t.entryDate),
          exitDate: toDatetimeLocal(t.exitDate),
          strategy: t.strategy ?? '',
          notes: t.notes ?? '',
        })
      })
    } else {
      // sensible default: now, in local datetime-local format
      const now = new Date()
      now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
      setForm((f) => ({ ...f, entryDate: now.toISOString().slice(0, 16) }))
    }
  }, [id])

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    const payload = {
      ...form,
      exitPrice: form.exitPrice === '' ? null : form.exitPrice,
      stopLoss: form.stopLoss === '' ? null : form.stopLoss,
      takeProfit: form.takeProfit === '' ? null : form.takeProfit,
      exitDate: form.exitDate === '' ? null : form.exitDate,
      status: form.exitPrice === '' ? 'OPEN' : 'CLOSED',
    }

    try {
      if (isEdit) {
        await api.put(`/trades/${id}`, payload)
      } else {
        await api.post('/trades', payload)
      }
      navigate('/trades')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save trade. Check the form fields.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>{isEdit ? 'Edit Trade' : 'Add Trade'}</h1>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <form className="card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="field">
            <label>Symbol</label>
            <input value={form.symbol} onChange={(e) => update('symbol', e.target.value)} placeholder="e.g. AAPL, BTCUSDT" required />
          </div>
          <div className="field">
            <label>Trade Type</label>
            <select value={form.tradeType} onChange={(e) => update('tradeType', e.target.value)}>
              <option value="LONG">Long</option>
              <option value="SHORT">Short</option>
            </select>
          </div>

          <div className="field">
            <label>Entry Price</label>
            <input type="number" step="0.0001" value={form.entryPrice} onChange={(e) => update('entryPrice', e.target.value)} required />
          </div>
          <div className="field">
            <label>Exit Price <span style={{ color: 'var(--text-faint)' }}>(leave blank if still open)</span></label>
            <input type="number" step="0.0001" value={form.exitPrice} onChange={(e) => update('exitPrice', e.target.value)} />
          </div>

          <div className="field">
            <label>Quantity</label>
            <input type="number" step="0.0001" value={form.quantity} onChange={(e) => update('quantity', e.target.value)} required />
          </div>
          <div className="field">
            <label>Strategy <span style={{ color: 'var(--text-faint)' }}>(optional)</span></label>
            <input value={form.strategy} onChange={(e) => update('strategy', e.target.value)} placeholder="e.g. Breakout, Scalping" />
          </div>

          <div className="field">
            <label>Stop Loss <span style={{ color: 'var(--text-faint)' }}>(optional)</span></label>
            <input type="number" step="0.0001" value={form.stopLoss} onChange={(e) => update('stopLoss', e.target.value)} />
          </div>
          <div className="field">
            <label>Take Profit <span style={{ color: 'var(--text-faint)' }}>(optional)</span></label>
            <input type="number" step="0.0001" value={form.takeProfit} onChange={(e) => update('takeProfit', e.target.value)} />
          </div>

          <div className="field">
            <label>Entry Date/Time</label>
            <input type="datetime-local" value={form.entryDate} onChange={(e) => update('entryDate', e.target.value)} required />
          </div>
          <div className="field">
            <label>Exit Date/Time <span style={{ color: 'var(--text-faint)' }}>(optional)</span></label>
            <input type="datetime-local" value={form.exitDate} onChange={(e) => update('exitDate', e.target.value)} />
          </div>
        </div>

        <div className="field">
          <label>Notes <span style={{ color: 'var(--text-faint)' }}>(optional)</span></label>
          <textarea rows={3} value={form.notes} onChange={(e) => update('notes', e.target.value)} placeholder="What was your thesis? What did you learn?" />
        </div>

        <div style={{ display: 'flex', gap: 12 }}>
          <button className="btn btn-primary" disabled={loading}>
            {loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Add Trade'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/trades')}>Cancel</button>
        </div>
      </form>
    </div>
  )
}
