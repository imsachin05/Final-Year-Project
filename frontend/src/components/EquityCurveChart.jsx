import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

export default function EquityCurveChart({ data }) {
  const isPositive = data.length > 0 && data[data.length - 1].cumulativePnl >= 0

  if (data.length === 0) {
    return (
      <div className="card" style={{ height: 320, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className="empty-state" style={{ padding: 0 }}>
          <h3>No closed trades yet</h3>
          <p>Your equity curve will appear here once you close a trade.</p>
        </div>
      </div>
    )
  }

  const lineColor = isPositive ? 'var(--gain)' : 'var(--loss)'

  return (
    <div className="card" style={{ height: 320 }}>
      <div style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 12, fontWeight: 600 }}>
        EQUITY CURVE
      </div>
      <ResponsiveContainer width="100%" height="88%">
        <AreaChart data={data} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
          <defs>
            <linearGradient id="equityFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={lineColor} stopOpacity={0.35} />
              <stop offset="95%" stopColor={lineColor} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
          <XAxis dataKey="date" tick={{ fill: 'var(--text-faint)', fontSize: 11 }} axisLine={{ stroke: 'var(--border)' }} tickLine={false} />
          <YAxis tick={{ fill: 'var(--text-faint)', fontSize: 11 }} axisLine={false} tickLine={false} />
          <Tooltip
            contentStyle={{ background: 'var(--surface-alt)', border: '1px solid var(--border)', borderRadius: 8, fontSize: 12 }}
            labelStyle={{ color: 'var(--text-muted)' }}
          />
          <Area type="monotone" dataKey="cumulativePnl" stroke={lineColor} strokeWidth={2} fill="url(#equityFill)" />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  )
}
