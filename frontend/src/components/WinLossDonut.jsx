import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts'

export default function WinLossDonut({ wins, losses }) {
  const total = wins + losses
  const data = [
    { name: 'Wins', value: wins },
    { name: 'Losses', value: losses },
  ]

  return (
    <div className="card" style={{ height: 320, display: 'flex', flexDirection: 'column' }}>
      <div style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 12, fontWeight: 600 }}>
        WIN / LOSS SPLIT
      </div>
      {total === 0 ? (
        <div className="empty-state" style={{ padding: '40px 0' }}>No closed trades yet</div>
      ) : (
        <div style={{ position: 'relative', flex: 1 }}>
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={data} dataKey="value" innerRadius="65%" outerRadius="90%" paddingAngle={3} stroke="none">
                <Cell fill="var(--gain)" />
                <Cell fill="var(--loss)" />
              </Pie>
            </PieChart>
          </ResponsiveContainer>
          <div style={{
            position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)',
            textAlign: 'center'
          }}>
            <div className="mono" style={{ fontSize: 26, fontWeight: 600 }}>
              {Math.round((wins / total) * 100)}%
            </div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>WIN RATE</div>
          </div>
        </div>
      )}
      <div style={{ display: 'flex', gap: 16, marginTop: 12, fontSize: 12, justifyContent: 'center' }}>
        <span><span style={{ color: 'var(--gain)' }}>●</span> Wins ({wins})</span>
        <span><span style={{ color: 'var(--loss)' }}>●</span> Losses ({losses})</span>
      </div>
    </div>
  )
}
