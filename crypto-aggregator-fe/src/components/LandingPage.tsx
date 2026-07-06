import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Activity, ArrowRight, Database, Lock, X, Zap} from 'lucide-react';

type ViewState = 'landing' | 'login' | 'register';

export default function LandingPage() {
  const [view, setView] = useState<ViewState>('landing');
  const navigate = useNavigate();

  // Стан форм
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // --- ЛОГІКА АВТОРИЗАЦІЇ ---
  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const endpoint = view === 'login' ? '/api/auth/login' : '/api/auth/register';
    const payload = view === 'login'
      ? {email, password}
      : {email, password, firstName, lastName};

    try {
      const response = await fetch(`http://localhost:8080${endpoint}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        const data = await response.json();
        // Зберігаємо токени згідно зі схемою TokenResponse
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        // Переходимо на дашборд
        navigate('/app');
      } else {
        const errorData = await response.json();
        // Використовуємо поле message з ErrorResponse
        setError(errorData.message || 'Authentication failed');
      }
    } catch {
      setError('Network error. Please check if the server is running.');
    } finally {
      setIsLoading(false);
    }
  };

  // --- КОМПОНЕНТИ ---

  // 1. Форма (Логін або Реєстрація)
  const renderAuthForm = () => (
    <div className="min-h-screen bg-[#0b0e14] flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-md bg-[#181a20] rounded-xl border border-[#2b3139] p-8 shadow-2xl relative">
        <button
          onClick={() => setView('landing')}
          className="absolute top-4 right-4 text-[#848e9c] hover:text-[#eaecef] transition-colors"
        >
          <X size={20}/>
        </button>

        <div className="flex justify-center mb-6">
          <div className="w-12 h-12 bg-[#fcd535]/10 rounded-full flex items-center justify-center">
            <Lock className="text-[#fcd535]" size={24}/>
          </div>
        </div>

        <h2 className="text-2xl font-bold text-[#eaecef] text-center mb-6">
          {view === 'login' ? 'Welcome Back' : 'Create an Account'}
        </h2>

        {error && (
          <div
            className="mb-4 p-3 bg-[#f6465d]/10 border border-[#f6465d]/50 rounded text-[#f6465d] text-sm text-center">
            {error}
          </div>
        )}

        <form onSubmit={handleAuth} className="space-y-4">
          {view === 'register' && (
            <div className="flex gap-4">
              <div className="flex-1">
                <label className="block text-xs text-[#848e9c] mb-1.5">First Name</label>
                <input
                  type="text"
                  required
                  minLength={2}
                  maxLength={50}
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="w-full bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] px-3 py-2 rounded focus:outline-none focus:border-[#fcd535] transition-colors"
                />
              </div>
              <div className="flex-1">
                <label className="block text-xs text-[#848e9c] mb-1.5">Last Name</label>
                <input
                  type="text"
                  required
                  minLength={2}
                  maxLength={50}
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="w-full bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] px-3 py-2 rounded focus:outline-none focus:border-[#fcd535] transition-colors"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-xs text-[#848e9c] mb-1.5">Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] px-3 py-2 rounded focus:outline-none focus:border-[#fcd535] transition-colors"
            />
          </div>

          <div>
            <label className="block text-xs text-[#848e9c] mb-1.5">Password</label>
            <input
              type="password"
              required
              minLength={6}
              maxLength={100}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] px-3 py-2 rounded focus:outline-none focus:border-[#fcd535] transition-colors"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full bg-[#fcd535] text-[#0b0e14] font-semibold py-2.5 rounded hover:bg-[#e0bc2e] transition-colors disabled:opacity-50 mt-6"
          >
            {isLoading ? 'Processing...' : (view === 'login' ? 'Log In' : 'Sign Up')}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-[#848e9c]">
          {view === 'login' ? "Don't have an account? " : "Already have an account? "}
          <button
            onClick={() => setView(view === 'login' ? 'register' : 'login')}
            className="text-[#fcd535] hover:underline"
          >
            {view === 'login' ? 'Register here' : 'Log in here'}
          </button>
        </div>
      </div>
    </div>
  );

  // 2. Головна сторінка (Landing)
  if (view !== 'landing') return renderAuthForm();

  return (
    <div className="min-h-screen bg-[#0b0e14] text-[#eaecef] font-sans selection:bg-[#fcd535] selection:text-[#0b0e14]">
      {/* Навігація */}
      <nav className="border-b border-[#2b3139] bg-[#181a20]">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Activity className="text-[#fcd535]" size={24}/>
            <span className="font-bold text-lg tracking-wide">CryptoAggregator</span>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={() => setView('login')}
              className="text-sm font-medium text-[#848e9c] hover:text-[#eaecef] transition-colors"
            >
              Log In
            </button>
            <button
              onClick={() => setView('register')}
              className="text-sm font-medium bg-[#fcd535] text-[#0b0e14] px-4 py-2 rounded hover:bg-[#e0bc2e] transition-colors"
            >
              Get Started
            </button>
          </div>
        </div>
      </nav>

      {/* Герой-секція */}
      <main className="max-w-7xl mx-auto px-6 pt-24 pb-16 flex flex-col items-center text-center">
        <div
          className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#fcd535]/10 text-[#fcd535] text-xs font-semibold mb-8 border border-[#fcd535]/20">
          <span className="w-2 h-2 rounded-full bg-[#0ecb81] animate-pulse"/>
          v1.0.0 API LIVE
        </div>

        <h1 className="text-5xl md:text-6xl font-extrabold tracking-tight mb-6 max-w-4xl">
          The Ultimate Unified <br/>
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#fcd535] to-[#f6a000]">
            Crypto Exchange API
          </span>
        </h1>

        <p className="text-lg text-[#848e9c] mb-10 max-w-2xl">
          Connect to Binance, Coinbase, and Kraken through a single, powerful interface.
          Access lightning-fast live SSE streams, comprehensive historical klines, and unified metadata.
        </p>

        <div className="flex items-center gap-4">
          <button
            onClick={() => setView('register')}
            className="flex items-center gap-2 bg-[#fcd535] text-[#0b0e14] font-bold px-8 py-3.5 rounded hover:bg-[#e0bc2e] transition-all transform hover:scale-105"
          >
            Start Building Free
            <ArrowRight size={18}/>
          </button>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="px-8 py-3.5 rounded font-semibold text-[#eaecef] bg-[#2b3139] hover:bg-[#474d57] transition-colors"
          >
            View Documentation
          </a>
        </div>

        {/* Переваги (Features) */}
        <div className="grid md:grid-cols-3 gap-6 mt-24 text-left w-full">
          <div
            className="bg-[#181a20] border border-[#2b3139] p-6 rounded-xl hover:border-[#fcd535]/50 transition-colors">
            <div className="w-10 h-10 bg-[#0ecb81]/10 rounded flex items-center justify-center mb-4">
              <Zap className="text-[#0ecb81]" size={20}/>
            </div>
            <h3 className="text-lg font-bold mb-2">Live SSE Streams</h3>
            <p className="text-sm text-[#848e9c] leading-relaxed">
              Consume real-time price updates and 24h tickers via Server-Sent Events with automatic reconnection
              handling.
            </p>
          </div>

          <div
            className="bg-[#181a20] border border-[#2b3139] p-6 rounded-xl hover:border-[#fcd535]/50 transition-colors">
            <div className="w-10 h-10 bg-[#3b82f6]/10 rounded flex items-center justify-center mb-4">
              <Activity className="text-[#3b82f6]" size={20}/>
            </div>
            <h3 className="text-lg font-bold mb-2">Historical Klines</h3>
            <p className="text-sm text-[#848e9c] leading-relaxed">
              Fetch deeply paginated historical candlestick data across 17 different time intervals for precise
              charting.
            </p>
          </div>

          <div
            className="bg-[#181a20] border border-[#2b3139] p-6 rounded-xl hover:border-[#fcd535]/50 transition-colors">
            <div className="w-10 h-10 bg-[#a855f7]/10 rounded flex items-center justify-center mb-4">
              <Database className="text-[#a855f7]" size={20}/>
            </div>
            <h3 className="text-lg font-bold mb-2">Unified Metadata</h3>
            <p className="text-sm text-[#848e9c] leading-relaxed">
              Standardized endpoints for discovering supported trading pairs and health statuses across all integrated
              exchanges.
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}