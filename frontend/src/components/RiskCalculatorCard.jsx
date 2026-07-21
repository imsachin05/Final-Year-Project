import { useState } from 'react'
import api from '../api'

export default function RiskCalculatorCard() {
  const [form, setForm] = useState({
    accountBalance: '10000',
    riskPercentage: '1',
    entryPrice: '',
    stopLossPrice: '',
    takeProfitPrice: '',
  })
  const [result, setResult] = useState(null)
  const [tip, setTip] = useState('')
  const [loading, setLoading] = useState(false)
  const [tipLoading, setTipLoading] = useState(false)
  const [error, setError] = useState('')

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
    setResult(null)
    setTip('')
  }

  function payload() {
    return {
      ...form,
      takeProfitPrice: form.takeProfitPrice === '' ? null : form.takeProfitPrice,
    }
  }

  async function calculate(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/risk/calculate', payload())
      setResult(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not calculate. Check that entry and stop loss differ.')
    } finally {
      setLoading(false)
    }
  }

  async function getTip() {
    setTipLoading(true)
    try {
      const res = await api.post('/ai/risk-tip', payload())
      setTip(res.data.message)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not get an AI tip right now.')
    } finally {
      setTipLoading(false)
    }
  }

  return (
    <div className="card">
      <div style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 600, marginBottom: 4 }}>
        AI RISK CALCULATOR
      </div>
      <div style={{ fontSize: 12, color: 'var(--text-faint)', marginBottom: 14 }}>
        Position sizing based on account risk % — pure math, always works even without an AI key
      </div>

      <form onSubmit={calculate}>
        <div className="form-grid">
          <div className="field">
            <label>Account Balance ($)</label>
            <input type="number" step="0.01" value={form.accountBalance} onChange={(e) => update('accountBalance', e.target.value)} required />
          </div>
          <div className="field">
            <label>Risk % Per Trade</label>
            <input type="number" step="0.1" value={form.riskPercentage} onChange={(e) => update('riskPercentage', e.target.value)} required />
          </div>
          <div className="field">
            <label>Entry Price</label>
            <input type="number" step="0.0001" value={form.entryPrice} onChange={(e) => update('entryPrice', e.target.value)} required />
          </div>
          <div className="field">
            <label>Stop Loss Price</label>
            <input type="number" step="0.0001" value={form.stopLossPrice} onChange={(e) => update('stopLossPrice', e.target.value)} required />
          </div>
          <div className="field">
            <label>Take Profit <span style={{ color: 'var(--text-faint)' }}>(optional)</span></label>
            <input type="number" step="0.0001" value={form.takeProfitPrice} onChange={(e) => update('takeProfitPrice', e.target.value)} />
          </div>
        </div>
        <button className="btn btn-primary" disabled={loading}>{loading ? 'Calculating…' : 'Calculate Position Size'}</button>
      </form>

      {error && <div className="error-banner" style={{ marginTop: 14 }}>{error}</div>}

      {result && (
        <>
          <div className="stat-grid" style={{ marginTop: 18, marginBottom: 10 }}>
            <div className="stat-card">
              <div className="stat-label">Risk Amount</div>
              <div className="stat-value mono loss">${Number(result.riskAmount).toFixed(2)}</div>
            </div>
            <div className="stat-card">
              <div className="stat-label">Suggested Quantity</div>
              <div className="stat-value mono">{Number(result.suggestedQuantity).toFixed(4)}</div>
            </div>
            <div className="stat-card">
              <div className="stat-label">Position Value</div>
              <div className="stat-value mono">${Number(result.positionValue).toFixed(2)}</div>
            </div>
            {result.riskRewardRatio && (
              <div className="stat-card">
                <div className="stat-label">Risk:Reward</div>
                <div className="stat-value mono gain">1:{result.riskRewardRatio}</div>
              </div>
            )}
          </div>

          <button className="btn btn-secondary" onClick={getTip} disabled={tipLoading}>
            {tipLoading ? 'Thinking…' : 'Get AI Tip on This Setup'}
          </button>

          {tip && (
            <div style={{ marginTop: 12, fontSize: 13.5, lineHeight: 1.6 }}>{tip}</div>
          )}
        </>
      )}
    </div>
  )
}
