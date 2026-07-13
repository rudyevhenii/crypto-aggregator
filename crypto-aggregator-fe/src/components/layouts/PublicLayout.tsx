import {Outlet} from 'react-router-dom';

export default function PublicLayout() {
  return (
    <div className="min-h-screen bg-[#09090b] relative">
      <Outlet />
    </div>
  );
}
