import PageHeader from '../../components/PageHeader.jsx';

const GUIDES = [
  {
    title: 'Getting started',
    body: [
      'Campaigns hold your objective, flight dates and budget. Create one from Campaigns → Create Campaign.',
      'Placements describe where ads run: country, audience group, traffic (app or website), position, device and format.',
      'Assign placements while creating a campaign, either by selecting existing ones or creating a new one inline.',
    ],
  },
  {
    title: 'Buying inventory',
    body: [
      'Inventory lists deals offered by publishers with their floor CPM and available impressions.',
      'Place a bid at or above the floor to win the deal. The deal is marked Sold and your campaign’s ads start serving.',
      'Bids below the floor are rejected and the deal remains available.',
    ],
  },
  {
    title: 'Roles and permissions',
    body: [
      'Administrator — full access including settings, deleting campaigns and activating audiences.',
      'Campaign manager — create and edit campaigns and placements, and bid on inventory.',
      'Viewer — read-only access to all pages.',
    ],
  },
];

const ENDPOINTS = [
  ['POST', '/api/auth/login', 'Exchange username and password for a bearer token'],
  ['GET', '/api/campaigns', 'List campaigns (status, objective, name, from, to, page, size)'],
  ['POST', '/api/campaigns', 'Create a campaign with placementIds and/or newPlacement'],
  ['PUT', '/api/campaigns/{id}', 'Update a campaign'],
  ['POST', '/api/placements', 'Create a placement'],
  ['GET', '/api/audiences', 'List audience segments (q, type, status)'],
  ['GET', '/api/deals', 'List inventory deals (q, type, status)'],
  ['POST', '/api/deals/{id}/bids', 'Bid on a deal: { amount, campaignId }'],
  ['GET', '/api/lookups', 'Enumerations and labels used by the UI'],
];

export default function DocsPage() {
  return (
    <>
      <PageHeader title="Documentation" subtitle="User guides and API reference" backTo="/help" backLabel="Help" />
      {GUIDES.map((g) => (
        <section key={g.title} className="card prose">
          <h2 className="section-title">{g.title}</h2>
          <ul>
            {g.body.map((line) => (
              <li key={line}>{line}</li>
            ))}
          </ul>
        </section>
      ))}
      <section className="card">
        <h2 className="section-title">REST API</h2>
        <p className="muted">All endpoints except login require <code>Authorization: Bearer &lt;token&gt;</code>.</p>
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Method</th>
                <th>Path</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              {ENDPOINTS.map(([m, p, d]) => (
                <tr key={m + p}>
                  <td><span className="method">{m}</span></td>
                  <td><code>{p}</code></td>
                  <td className="muted">{d}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
