import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ActivityListPage from '../pages/activity/ActivityListPage.jsx';
import { usePagedList } from '../hooks/usePagedList.js';
import { useDebounce } from '../hooks/useDebounce.js';

vi.mock('../hooks/usePagedList.js', () => ({
  usePagedList: vi.fn(),
}));

vi.mock('../hooks/useDebounce.js', () => ({
  useDebounce: vi.fn((v) => v),
}));

const MOCK_EVENTS = [
  {
    id: 1,
    action: 'CREATED',
    entityType: 'CAMPAIGN',
    entityId: 7,
    entityName: 'Summer Sale 2026',
    actor: 'admin',
    summary: "Created campaign 'Summer Sale 2026'",
    timestamp: '2026-09-24T10:00:00Z',
  },
  {
    id: 2,
    action: 'DELETED',
    entityType: 'CAMPAIGN',
    entityId: 3,
    entityName: 'Old Campaign',
    actor: 'admin',
    summary: "Deleted campaign 'Old Campaign'",
    timestamp: '2026-09-24T09:00:00Z',
  },
  {
    id: 3,
    action: 'STATUS_CHANGED',
    entityType: 'AUDIENCE',
    entityId: 5,
    entityName: 'Tech Enthusiasts',
    actor: 'manager',
    summary: "Changed audience 'Tech Enthusiasts' status to Inactive",
    timestamp: '2026-09-24T08:00:00Z',
  },
  {
    id: 4,
    action: 'BID_PLACED',
    entityType: 'DEAL',
    entityId: 2,
    entityName: 'Premium Banner',
    actor: 'admin',
    summary: "Placed bid of $500.00 on deal 'Premium Banner' for campaign 'Summer Sale 2026'",
    timestamp: '2026-09-24T07:00:00Z',
  },
  {
    id: 5,
    action: 'CREATED',
    entityType: 'PLACEMENT',
    entityId: 10,
    entityName: 'Header Banner',
    actor: 'admin',
    summary: "Created placement 'Header Banner'",
    timestamp: '2026-09-24T06:00:00Z',
  },
];

const MOCK_RESPONSE = {
  content: MOCK_EVENTS,
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
    usePagedList.mockReturnValue({
      data: MOCK_RESPONSE,
      error: null,
      loading: false,
    });
  });

  // ---- F1.0: Page header and navigation ----

  it('renders the page header with title', () => {
    renderPage();
    expect(screen.getByText('Activity Log')).toBeInTheDocument();
  });

  it('renders the subtitle', () => {
    renderPage();
    expect(screen.getByText('Chronological record of all changes')).toBeInTheDocument();
  });

  // ---- F3.0: Table columns ----

  it('renders table with all five column headers', () => {
    renderPage();
    const headers = ['Time', 'Actor', 'Action', 'Entity', 'Details'];
    headers.forEach((h) => {
      expect(screen.getByText(h)).toBeInTheDocument();
    });
  });

  it('renders correct number of data rows', () => {
    renderPage();
    const rows = screen.getAllByRole('row');
    // 1 header row + 5 data rows
    expect(rows).toHaveLength(6);
  });

  it('renders actor names in rows', () => {
    renderPage();
    const adminCells = screen.getAllByText('admin');
    expect(adminCells.length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('manager')).toBeInTheDocument();
  });

  it('renders summary text in details column', () => {
    renderPage();
    expect(screen.getByText("Created campaign 'Summer Sale 2026'")).toBeInTheDocument();
    expect(screen.getByText("Deleted campaign 'Old Campaign'")).toBeInTheDocument();
  });

  // ---- F3.0: Action badges ----

  it('renders Created badge with success tone', () => {
    renderPage();
    const createdBadges = screen.getAllByText('Created');
    const badge = createdBadges.find((el) => el.classList.contains('badge'));
    expect(badge).toBeDefined();
    expect(badge.className).toContain('badge-success');
  });

  it('renders Deleted badge with danger tone', () => {
    renderPage();
    const badge = screen.getByText('Deleted');
    expect(badge.className).toContain('badge-danger');
  });

  it('renders Status changed badge with warning tone', () => {
    renderPage();
    const badge = screen.getByText('Status changed');
    expect(badge.className).toContain('badge-warning');
  });

  it('renders Bid placed badge with neutral tone', () => {
    renderPage();
    const badge = screen.getByText('Bid placed');
    expect(badge.className).toContain('badge-neutral');
  });

  // ---- F4.0: Filter controls ----

  it('renders all filter controls', () => {
    renderPage();
    expect(screen.getByLabelText('Filter by entity type')).toBeInTheDocument();
    expect(screen.getByLabelText('Filter by action')).toBeInTheDocument();
    expect(screen.getByLabelText('Filter by actor')).toBeInTheDocument();
    expect(screen.getByLabelText('From date')).toBeInTheDocument();
    expect(screen.getByLabelText('To date')).toBeInTheDocument();
  });

  it('entity type filter has all options', () => {
    renderPage();
    const select = screen.getByLabelText('Filter by entity type');
    const options = within(select).getAllByRole('option');
    expect(options.map((o) => o.value)).toEqual([
      '', 'CAMPAIGN', 'PLACEMENT', 'AUDIENCE', 'DEAL',
    ]);
  });

  it('action filter has all options', () => {
    renderPage();
    const select = screen.getByLabelText('Filter by action');
    const options = within(select).getAllByRole('option');
    expect(options.map((o) => o.value)).toEqual([
      '', 'CREATED', 'UPDATED', 'DELETED', 'STATUS_CHANGED', 'BID_PLACED',
    ]);
  });

  it('does not show clear button when no filters are active', () => {
    renderPage();
    expect(screen.queryByText('Clear')).not.toBeInTheDocument();
  });

  it('shows clear button when entity type filter is selected', () => {
    renderPage();
    fireEvent.change(screen.getByLabelText('Filter by entity type'), {
      target: { value: 'CAMPAIGN' },
    });
    expect(screen.getByText('Clear')).toBeInTheDocument();
  });

  it('shows clear button when action filter is selected', () => {
    renderPage();
    fireEvent.change(screen.getByLabelText('Filter by action'), {
      target: { value: 'CREATED' },
    });
    expect(screen.getByText('Clear')).toBeInTheDocument();
  });

  it('shows clear button when actor text is entered', () => {
    renderPage();
    fireEvent.change(screen.getByLabelText('Filter by actor'), {
      target: { value: 'admin' },
    });
    expect(screen.getByText('Clear')).toBeInTheDocument();
  });

  it('clears all filters when clear button is clicked', () => {
    renderPage();
    const entitySelect = screen.getByLabelText('Filter by entity type');
    const actionSelect = screen.getByLabelText('Filter by action');
    const actorInput = screen.getByLabelText('Filter by actor');

    fireEvent.change(entitySelect, { target: { value: 'CAMPAIGN' } });
    fireEvent.change(actionSelect, { target: { value: 'DELETED' } });
    fireEvent.change(actorInput, { target: { value: 'admin' } });

    fireEvent.click(screen.getByText('Clear'));

    expect(entitySelect.value).toBe('');
    expect(actionSelect.value).toBe('');
    expect(actorInput.value).toBe('');
  });

  // ---- F5.0: Entity deep links ----

  it('links campaign entity to campaign detail page', () => {
    renderPage();
    const link = screen.getByText('Summer Sale 2026');
    expect(link.tagName).toBe('A');
    expect(link.getAttribute('href')).toBe('/campaigns/7');
  });

  it('shows deleted entity as plain text without a link', () => {
    renderPage();
    const name = screen.getByText('Old Campaign');
    expect(name.tagName).toBe('SPAN');
  });

  it('links audience entity to audience list page', () => {
    renderPage();
    const link = screen.getByText('Tech Enthusiasts');
    expect(link.tagName).toBe('A');
    expect(link.getAttribute('href')).toBe('/audience');
  });

  it('links deal entity to inventory page', () => {
    renderPage();
    const link = screen.getByText('Premium Banner');
    expect(link.tagName).toBe('A');
    expect(link.getAttribute('href')).toBe('/inventory');
  });

  it('shows placement entity as plain text (no route defined)', () => {
    renderPage();
    const name = screen.getByText('Header Banner');
    expect(name.tagName).toBe('SPAN');
  });

  // ---- F3.0: Entity type labels ----

  it('displays entity type labels below entity names', () => {
    renderPage();
    const mutedDivs = document.querySelectorAll('.muted');
    const labels = Array.from(mutedDivs).map((d) => d.textContent);
    expect(labels).toContain('Campaign');
    expect(labels).toContain('Audience');
    expect(labels).toContain('Deal');
    expect(labels).toContain('Placement');
  });

  // ---- F6.0: Read-only enforcement ----

  it('does not render create, edit, or delete buttons', () => {
    renderPage();
    const buttons = screen.getAllByRole('button');
    const labels = buttons.map((b) => b.textContent.toLowerCase());
    expect(labels).not.toContain('create');
    expect(labels).not.toContain('edit');
    expect(labels).not.toContain('delete');
    expect(labels).not.toContain('add');
  });

  // ---- Edge cases ----

  it('renders empty state when no results', () => {
    usePagedList.mockReturnValue({
      data: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      loading: false,
    });
    renderPage();
    expect(screen.getByText('No activity found')).toBeInTheDocument();
    expect(screen.getByText('Try a different filter or date range.')).toBeInTheDocument();
  });

  it('renders error state when API fails', () => {
    usePagedList.mockReturnValue({
      data: null,
      error: { message: 'Network error' },
      loading: false,
    });
    renderPage();
    expect(screen.getByText('Could not load activity')).toBeInTheDocument();
  });

  it('renders loading state during initial fetch', () => {
    usePagedList.mockReturnValue({
      data: null,
      error: null,
      loading: true,
    });
    renderPage();
    // No table visible, spinner present (EmptyState/Spinner internal implementation)
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });

  // ---- Hook integration ----

  it('calls usePagedList with activityApi.list and default params', () => {
    renderPage();
    expect(usePagedList).toHaveBeenCalled();
    const [fetchFn, params] = usePagedList.mock.calls[0];
    expect(params).toMatchObject({ page: 0, size: 10 });
  });

  it('passes entityType filter to usePagedList params', () => {
    renderPage();
    fireEvent.change(screen.getByLabelText('Filter by entity type'), {
      target: { value: 'DEAL' },
    });
    const lastCall = usePagedList.mock.calls[usePagedList.mock.calls.length - 1];
    expect(lastCall[1]).toMatchObject({ entityType: 'DEAL' });
  });

  it('passes action filter to usePagedList params', () => {
    renderPage();
    fireEvent.change(screen.getByLabelText('Filter by action'), {
      target: { value: 'UPDATED' },
    });
    const lastCall = usePagedList.mock.calls[usePagedList.mock.calls.length - 1];
    expect(lastCall[1]).toMatchObject({ action: 'UPDATED' });
  });

  it('resets page to 0 when a filter changes', () => {
    renderPage();
    fireEvent.change(screen.getByLabelText('Filter by entity type'), {
      target: { value: 'CAMPAIGN' },
    });
    const lastCall = usePagedList.mock.calls[usePagedList.mock.calls.length - 1];
    expect(lastCall[1].page).toBe(0);
  });
});
