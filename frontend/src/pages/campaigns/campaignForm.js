import { collect, dateOrder, maxLength, moneyRange, required } from '../../utils/validators.js';

export const EMPTY_CAMPAIGN = {
  name: '',
  objective: '',
  startDate: '',
  endDate: '',
  budget: '',
  status: 'DRAFT',
  description: '',
};

export const CAMPAIGN_DEPENDENCIES = { startDate: ['endDate'] };

export const validateCampaign = (v) =>
  collect({
    name: required(v.name, 'Campaign name') || maxLength(v.name, 120, 'Campaign name'),
    objective: required(v.objective, 'Objective'),
    startDate: required(v.startDate, 'Start date'),
    endDate: required(v.endDate, 'End date') || dateOrder(v.startDate, v.endDate),
    budget: required(v.budget, 'Budget') || moneyRange(v.budget, 100, 100000000, 'Budget'),
    status: required(v.status, 'Status'),
    description: maxLength(v.description, 1000, 'Description'),
  });

export const fromCampaign = (c) => ({
  name: c.name,
  objective: c.objective,
  startDate: c.startDate,
  endDate: c.endDate,
  budget: String(c.budget),
  status: c.status,
  description: c.description || '',
});
