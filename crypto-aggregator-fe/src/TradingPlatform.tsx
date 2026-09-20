import {Route, Routes} from 'react-router-dom';

import LandingPage from './components/LandingPage';
import PublicLayout from './components/layouts/PublicLayout';
import AppLayout from './components/layouts/AppLayout';
import OverviewRoute from './components/routes/OverviewRoute';
import ChartRoute from './components/routes/ChartRoute';
import WorkspaceRoute from './components/routes/WorkspaceRoute';
import {MarketDataProvider} from './contexts/MarketDataContext';

function App(): JSX.Element {
  return (
    <MarketDataProvider>
      <Routes>
        <Route path="/" element={<PublicLayout/>}>
          <Route index element={<LandingPage/>}/>
        </Route>

        <Route path="/app" element={<AppLayout/>}>
          <Route path="overview" element={<OverviewRoute/>}/>
          <Route path="chart/:exchange/:symbol" element={<ChartRoute/>}/>
          <Route path="workspace" element={<WorkspaceRoute/>}/>
        </Route>
      </Routes>
    </MarketDataProvider>
  );
}

export default App;
