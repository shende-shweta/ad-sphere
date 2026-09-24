import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ActivityListPage from '../pages/activity/ActivityListPage.jsx';
import { usePagedList } from '../hooks/usePagedList.js';

vi.mock('../hooks/usePagedList.js', () => ({
  usePagedList: vi.fn(),
}));

vi.mock('../hooks/useDebounce.js', () => ({
  useDebounce: (value) => value,
}));

// Activity Audit Log — ActivityListPage component tests (AC-C01 through AC-C08)
const MOCK_ACTIVITY = {
  content: [
    {
      id: 1,
      actor: 'admin',
      action: 'CREATED',
      entityType: 'CAMPAIGN',
      entityId: 7,
      entityName: 'Summer Sale 2026',
      summary: "Campaign 'Summer Sale 2026' created",
      timestamp: '2026-09-24T10:15:30Z',
    },
    {
      id: 2,
      actor: 'manager',
      action: 'DELETED',
      entityType: 'CAMPAIGN',
      entityId: 3,
      entityName: 'Old Campaign',
      summary: "Campaign 'Old Campaign' deleted",
      timestamp: '2026-09-24T09:00:00Z',
    },
    {
      id: 3,
      actor: 'admin',
      action: 'STATUS_CHANGED',
      entityType: 'AUDIENCE',
      entityId: 5,
      entityName: 'Tech Enthusiasts',
      summary: "Audience 'Tech Enthusiasts' status changed to INACTIVE",
      timestamp: '2026-09-24T08:30:00Z',
    },
    {
      id: 4,
      actor: 'admin',
      action: 'BID_PLACED',
      entityType: 'DEAL',
      entityId: 2,
      entityName: 'Premium Inventory Q4',
      summary: "Bid of 5000.00 placed on deal 'Premium Inventory Q4' by admin",
      timestamp: '2026-09-24T08:00:00Z',
    },
    {
      id: 5,
      actor: 'admin',
      action: 'UPDATED',
      entityType: 'CAMPAIGN',
      entityId: 7,
      entityName: 'Summer Sale 2026',
      summary: "Campaign 'Summer Sale 2026' updated",
      timestamp: '2026-09-24T07:30:00Z',
    },
  ],
  page: 0,
  size: 10,
  totalElements: 5,
  totalPages: 1,
};

function renderPage() {
  return render(
    <MemoryRouter>
      <ActivityListPage />
    </MemoryRouter>,
  );
}

describe('ActivityListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // AC-C01: Page renders with title
  it('renders page header with title', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    expect(screen.getByText('Activity Log')).toBeDefined();
  });

  // AC-C02: Table columns
  it('renders table with correct column headers', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    expect(screen.getByText('Time')).toBeDefined();
    expect(screen.getByText('Actor')).toBeDefined();
    expect(screen.getByText('Action')).toBeDefined();
    expect(screen.getByText('Entity')).toBeDefined();
    expect(screen.getByText('Details')).toBeDefined();
  });

  // AC-C03: Action type badges with labels for all 5 types
  it('renders action type badges with correct labels', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    expect(screen.getByText('Created')).toBeDefined();
    expect(screen.getByText('Deleted')).toBeDefined();
    expect(screen.getByText('Status Changed')).toBeDefined();
    expect(screen.getByText('Bid Placed')).toBeDefined();
    expect(screen.getByText('Updated')).toBeDefined();
  });

  // AC-C04: Deep link for non-deleted CAMPAIGN entity -> /campaigns/{id}
  it('renders deep link for non-deleted CAMPAIGN entity', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    const links = screen.getAllByText('Summer Sale 2026');
    const linkEl = links.find((el) => el.tagName === 'A');
    expect(linkEl).toBeDefined();
    expect(linkEl.getAttribute('href')).toBe('/campaigns/7');
  });

  // AC-C05: DELETED entity renders as plain text (no link)
  it('renders plain text for DELETED entity with no link', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    const deletedEl = screen.getByText('Old Campaign');
    expect(deletedEl.tagName).toBe('SPAN');
    expect(deletedEl.closest('a')).toBeNull();
  });

  // AC-C04: AUDIENCE deep link -> /audience
  it('renders deep link for AUDIENCE entity', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    const el = screen.getByText('Tech Enthusiasts');
    expect(el.tagName).toBe('A');
    expect(el.getAttribute('href')).toBe('/audience');
  });

  // AC-C04: DEAL deep link -> /inventory
  it('renders deep link for DEAL entity', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    const el = screen.getByText('Premium Inventory Q4');
    expect(el.tagName).toBe('A');
    expect(el.getAttribute('href')).toBe('/inventory');
  });

  // Entity type subtitles
  it('renders entity type as subtitle', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    const campaignSubs = screen.getAllByText('CAMPAIGN');
    expect(campaignSubs.length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('AUDIENCE').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('DEAL').length).toBeGreaterThanOrEqual(1);
  });

  // Summary text displayed
  it('displays summaries in table rows', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    expect(screen.getByText("Campaign 'Summer Sale 2026' created")).toBeDefined();
    expect(screen.getByText("Campaign 'Old Campaign' deleted")).toBeDefined();
    expect(
      screen.getByText("Audience 'Tech Enthusiasts' status changed to INACTIVE"),
    ).toBeDefined();
  });

  // AC-C08: Empty state when no results
  it('renders empty state when no activity found', () => {
    usePagedList.mockReturnValue({
      data: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      loading: false,
    });
    renderPage();
    expect(screen.getByText('No activity found')).toBeDefined();
  });

  // Error state
  it('renders error state on fetch failure', () => {
    usePagedList.mockReturnValue({
      data: null,
      error: new Error('Network error'),
      loading: false,
    });
    renderPage();
    expect(screen.getByText('Could not load activity')).toBeDefined();
  });

  // AC-C06: Date filter inputs present
  it('renders date filter inputs', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    expect(screen.getByText('From')).toBeDefined();
    expect(screen.getByText('To')).toBeDefined();
  });

  // AC-C06: Actor search input present
  it('renders actor search input', () => {
    usePagedList.mockReturnValue({ data: MOCK_ACTIVITY, error: null, loading: false });
    renderPage();
    expect(screen.getByPlaceholderText('Filter by actor...')).toBeDefined();
  });
});
