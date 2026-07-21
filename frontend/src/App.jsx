import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Trades from './pages/Trades'
import AddTrade from './pages/AddTrade'
import AiAssistant from './pages/AiAssistant'
import AiDashboard from './pages/AiDashboard'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/trades" element={<ProtectedRoute><Trades /></ProtectedRoute>} />
          <Route path="/trades/new" element={<ProtectedRoute><AddTrade /></ProtectedRoute>} />
          <Route path="/trades/:id/edit" element={<ProtectedRoute><AddTrade /></ProtectedRoute>} />
          <Route path="/ai" element={<ProtectedRoute><AiDashboard /></ProtectedRoute>} />
          <Route path="/assistant" element={<ProtectedRoute><AiAssistant /></ProtectedRoute>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
