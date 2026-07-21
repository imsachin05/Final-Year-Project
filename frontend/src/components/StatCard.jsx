export default function StatCard({ label, value, tone }) {
  const toneClass = tone === 'gain' ? 'gain' : tone === 'loss' ? 'loss' : ''
  return (
    <div className="stat-card">
      <div className="stat-label">{label}</div>
      <div className={`stat-value mono ${toneClass}`}>{value}</div>
    </div>
  )
}
