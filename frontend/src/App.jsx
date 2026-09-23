import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import RequireAuth from './auth/RequireAuth.jsx';
import { LookupsProvider } from './context/LookupsContext.jsx';
import LoginPage from './pages/LoginPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import CampaignListPage from './pages/campaigns/CampaignListPage.jsx';
import CampaignFormPage from './pages/campaigns/CampaignFormPage.jsx';
import CampaignDetailPage from './pages/campaigns/CampaignDetailPage.jsx';
import PlacementFormPage from './pages/placements/PlacementFormPage.jsx';
import AudienceListPage from './pages/audience/AudienceListPage.jsx';
import InventoryListPage from './pages/inventory/InventoryListPage.jsx';
import SettingsPage from './pages/settings/SettingsPage.jsx';
import SettingsSectionPage from './pages/settings/SettingsSectionPage.jsx';
import HelpPage from './pages/help/HelpPage.jsx';
import DocsPage from './pages/help/DocsPage.jsx';
import FaqPage from './pages/help/FaqPage.jsx';
import ContactSupportPage from './pages/help/ContactSupportPage.jsx';
import ProfilePage from './pages/profile/ProfilePage.jsx';
import EditProfilePage from './pages/profile/EditProfilePage.jsx';
import SecurityPage from './pages/profile/SecurityPage.jsx';
import { EDIT_ROLES } from './auth/roles.js';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <LookupsProvider>
              <Layout />
            </LookupsProvider>
          </RequireAuth>
        }
      >
        <Route index element={<Navigate to="/campaigns" replace />} />
        <Route path="campaigns" element={<CampaignListPage />} />
        <Route
          path="campaigns/new"
          element={
            <RequireAuth roles={EDIT_ROLES}>
              <CampaignFormPage />
            </RequireAuth>
          }
        />
        <Route path="campaigns/:id" element={<CampaignDetailPage />} />
        <Route
          path="campaigns/:id/edit"
          element={
            <RequireAuth roles={EDIT_ROLES}>
              <CampaignFormPage />
            </RequireAuth>
          }
        />
        <Route
          path="placements/new"
          element={
            <RequireAuth roles={EDIT_ROLES}>
              <PlacementFormPage />
            </RequireAuth>
          }
        />
        <Route path="audience" element={<AudienceListPage />} />
        <Route path="inventory" element={<InventoryListPage />} />
        <Route path="settings" element={<SettingsPage />} />
        <Route path="settings/:section" element={<SettingsSectionPage />} />
        <Route path="help" element={<HelpPage />} />
        <Route path="help/docs" element={<DocsPage />} />
        <Route path="help/faqs" element={<FaqPage />} />
        <Route path="help/contact" element={<ContactSupportPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="profile/edit" element={<EditProfilePage />} />
        <Route path="profile/security" element={<SecurityPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
