import {Activity, ArrowRight, Check, Lock, X} from 'lucide-react';
import {Button, Card, Input} from './ui';
import {useAuth} from '../hooks/useAuth';

export default function LandingPage() {
  const {view, setView, email, setEmail, password, setPassword, firstName, setFirstName, lastName, setLastName, error, isLoading, handleAuth} = useAuth();

  if (view !== 'landing') {
    return (
      <div className="min-h-screen bg-[#0b0e14] flex flex-col items-center justify-center p-4 relative overflow-hidden">
        {/* Ambient background glow */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#fcd535]/5 rounded-full blur-3xl"/>
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-[#3b82f6]/5 rounded-full blur-3xl"/>
        </div>

        <Card className="w-full max-w-md p-8 relative z-10">
          <button
            onClick={() => setView('landing')}
            className="absolute top-4 right-4 text-[#848e9c] hover:text-[#eaecef] transition-colors p-1 rounded-lg hover:bg-[#2b3139]/50"
            aria-label="Close"
          >
            <X size={20}/>
          </button>

          <div className="flex justify-center mb-6">
            <div className="w-12 h-12 bg-[#fcd535]/10 rounded-xl flex items-center justify-center border border-[#fcd535]/20">
              <Lock className="text-[#fcd535]" size={24}/>
            </div>
          </div>

          <h2 className="text-2xl font-bold text-[#eaecef] text-center mb-1 tracking-tight">
            {view === 'login' ? 'Welcome Back' : 'Create an Account'}
          </h2>
          <p className="text-sm text-[#848e9c] text-center mb-6">
            {view === 'login' ? 'Sign in to your account' : 'Get started with CryptoAggregator'}
          </p>

          {error && (
            <div className="mb-4 p-3 bg-[#f6465d]/10 border border-[#f6465d]/30 rounded-lg text-[#f6465d] text-sm text-center">
              {error}
            </div>
          )}

          <form onSubmit={handleAuth} className="space-y-4">
            {view === 'register' && (
              <div className="flex gap-3">
                <div className="flex-1">
                  <Input
                    label="First Name"
                    type="text"
                    required
                    minLength={2}
                    maxLength={50}
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    placeholder="John"
                  />
                </div>
                <div className="flex-1">
                  <Input
                    label="Last Name"
                    type="text"
                    required
                    minLength={2}
                    maxLength={50}
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    placeholder="Doe"
                  />
                </div>
              </div>
            )}

            <Input
              label="Email Address"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
            />

            <Input
              label="Password"
              type="password"
              required
              minLength={6}
              maxLength={100}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />

            <Button
              type="submit"
              isLoading={isLoading}
              className="w-full mt-6"
              size="lg"
            >
              {view === 'login' ? 'Log In' : 'Sign Up'}
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-[#848e9c]">
            {view === 'login' ? "Don't have an account? " : "Already have an account? "}
            <button
              onClick={() => setView(view === 'login' ? 'register' : 'login')}
              className="text-[#fcd535] hover:underline font-medium transition-colors"
            >
              {view === 'login' ? 'Register here' : 'Log in here'}
            </button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#09090b] text-zinc-50 font-sans selection:bg-[#fcd535] selection:text-[#09090b] relative overflow-hidden">
      {/* Ambient background effects */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-[#fcd535]/[0.04] rounded-full blur-3xl"/>
        <div className="absolute top-1/3 left-1/4 w-[400px] h-[400px] bg-[#3b82f6]/[0.03] rounded-full blur-3xl"/>
        <div className="absolute bottom-0 right-1/4 w-[500px] h-[500px] bg-[#a855f7]/[0.02] rounded-full blur-3xl"/>
        {/* Grid pattern overlay */}
        <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PHBhdHRlcm4gaWQ9ImEiIHBhdHRlcm5Vbml0cz0idXNlclNwYWNlT25Vc2UiIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgcGF0dGVyblRyYW5zZm9ybT0icm90YXRlKDkwKSI+PHBhdGggZD0iTTAgNDBMMDQgMEgwIiBmaWxsPSJub25lIiBzdHJva2U9InJnYmEoMjU1LDI1NSwyNTUsMC4wMykiIHN0cm9rZS13aWR0aD0iMSIvPjwvcGF0dGVybj48L2RlZnM+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0idXJsKCNhKSIvPjwvc3ZnPg==')] opacity-40"/>
      </div>

      {/* Navigation */}
      <nav className="relative z-50 border-b border-white/5 glass-surface">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 bg-[#fcd535]/10 rounded-lg flex items-center justify-center border border-[#fcd535]/20">
              <Activity className="text-[#fcd535]" size={20}/>
            </div>
            <span className="font-bold text-lg tracking-wide text-zinc-50">CryptoAggregator</span>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => setView('login')}
              className="text-sm font-medium text-zinc-400 hover:text-zinc-50 transition-colors px-3 py-1.5 rounded-lg hover:bg-white/5"
            >
              Log In
            </button>
            <Button size="sm" onClick={() => setView('register')} className="shadow-[0_0_20px_rgba(252,213,53,0.3)]">
              Get Started
            </Button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="relative z-10 max-w-7xl mx-auto px-6 pt-24 pb-16 flex flex-col items-center text-center">
        <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-6 max-w-4xl leading-[1.1]">
          The Ultimate Unified <br/>
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#fcd535] via-[#f6a000] to-[#fcd535] bg-[length:200%_auto] animate-[gradient_3s_linear_infinite]">
            Crypto Exchange API
          </span>
        </h1>

        <p className="text-lg md:text-xl text-zinc-400 mb-10 max-w-2xl leading-relaxed">
          Connect to Binance, Coinbase, and Kraken through a single, powerful interface.
          Access lightning-fast live SSE streams, comprehensive historical klines, and unified metadata.
        </p>

        <div className="flex items-center gap-4">
          <Button
            size="lg"
            onClick={() => setView('register')}
            rightIcon={<ArrowRight size={18}/>}
            className="shadow-[0_0_30px_rgba(252,213,53,0.2)] hover:shadow-[0_0_40px_rgba(252,213,53,0.3)]"
          >
            Start Building Free
          </Button>
        </div>

        {/* Feature Showcase - Zig-Zag Layout */}
        <div className="mt-32 space-y-32 w-full">
          {[
            {
              title: 'Real-Time Market Overview',
              description: 'Monitor the pulse of the crypto market with our unified dashboard. Instantly switch between Binance, Coinbase, and Kraken to track live prices, 24h changes, and quick sparkline trends.',
              features: ['Real-time SSE price updates', 'Cross-exchange aggregation', 'Instant visual trend indicators'],
              color: '#0ecb81',
              imagePosition: 'right',
              video: '/dashboard.webm',
            },
            {
              title: 'Deep Historical Charting',
              description: 'Dive deep into price action with powerful, interactive charts. Analyze historical candlestick data across multiple time intervals, complete with real-time 24h high/low and volume tracking.',
              features: ['Seamless historical data pagination', 'Interactive candlestick analysis', 'Detailed 24h price overview panel'],
              color: '#3b82f6',
              imagePosition: 'left',
              video: '/chart.webm',
            },
            {
              title: 'Advanced Multi-Chart Workspaces',
              description: 'Never miss a macro or micro trend. Create custom grid workspaces to compare multiple timeframes—like 5m, 15m, 30m, and 1h—side-by-side simultaneously on a single screen.',
              features: ['Customizable multi-chart grids', 'Synchronized timeframe comparison', 'Save and manage custom workspaces'],
              color: '#fcd535',
              imagePosition: 'right',
              video: '/workspace.webm',
            },
          ].map((feature, index) => (
            <div
              key={index}
              className={`flex flex-col ${feature.imagePosition === 'right' ? 'md:flex-row' : 'md:flex-row-reverse'} gap-12 items-center`}
            >
              {/* Text Content */}
              <div className="flex-1 space-y-6">
                <h2 className="text-3xl md:text-4xl font-bold text-zinc-50 tracking-tight">
                  {feature.title}
                </h2>
                <p className="text-lg text-zinc-400 leading-relaxed">
                  {feature.description}
                </p>
                <ul className="space-y-3">
                  {feature.features.map((item, i) => (
                    <li key={i} className="flex items-center gap-3 text-zinc-300">
                      <div
                        className="w-5 h-5 rounded-full flex items-center justify-center"
                        style={{backgroundColor: `${feature.color}20`}}
                      >
                        <Check size={12} style={{color: feature.color}}/>
                      </div>
                      <span className="text-sm">{item}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Video Placeholder */}
              <div className="flex-1 relative w-full lg:max-w-[550px] xl:max-w-[650px] mx-auto flex-shrink-0">
                {/* Ambient Glow */}
                <div
                  className="absolute -inset-4 rounded-2xl opacity-60 blur-2xl transition-all duration-500"
                  style={{ background: `radial-gradient(circle at center, ${feature.color}20, transparent 70%)` }}
                />
                
                {/* Standardized Aspect Ratio Container */}
                <div className="relative w-full aspect-video rounded-xl border border-white/10 bg-[#0b0e14] overflow-hidden shadow-[0_0_40px_rgba(0,0,0,0.5)] group">
                  {feature.video ? (
                    <video
                      autoPlay
                      loop
                      muted
                      playsInline
                      className="w-full h-full object-cover object-top opacity-85 group-hover:opacity-100 transition-all duration-700 ease-out"
                    >
                      <source src={feature.video} type="video/webm" />
                    </video>
                  ) : null}
                </div>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
