import { Bell, Plug, Settings2, ShieldCheck } from 'lucide-react';
import { collect, httpsUrl, maxLength, required } from '../../utils/validators.js';

export const TIMEZONES = ['UTC', 'America/New_York', 'America/Los_Angeles', 'Europe/London', 'Europe/Berlin', 'Asia/Kolkata', 'Asia/Singapore', 'Asia/Tokyo', 'Australia/Sydney'];
export const LANGUAGES = [
  { value: 'en-US', label: 'English (US)' },
  { value: 'en-GB', label: 'English (UK)' },
  { value: 'de-DE', label: 'Deutsch' },
  { value: 'fr-FR', label: 'Français' },
  { value: 'ja-JP', label: '日本語' },
];
export const CURRENCIES = ['USD', 'EUR', 'GBP', 'INR', 'JPY', 'AUD', 'CAD', 'SGD'];

export const SECTIONS = {
  general: {
    title: 'General Settings',
    description: 'Application name, timezone, language, etc.',
    icon: Settings2,
  },
  integrations: {
    title: 'Integrations',
    description: 'Connect with third-party services',
    icon: Plug,
  },
  notifications: {
    title: 'Notifications',
    description: 'Email and system notifications',
    icon: Bell,
  },
  security: {
    title: 'Security',
    description: 'Password, 2FA, session management',
    icon: ShieldCheck,
  },
};

export const validateSettings = (v) => {
  const timeout = Number(v.sessionTimeoutMinutes);
  const minLen = Number(v.passwordMinLength);
  return collect({
    platformName: required(v.platformName, 'Platform name') || maxLength(v.platformName, 80, 'Platform name'),
    webhookUrl: httpsUrl(v.webhookUrl),
    sessionTimeoutMinutes:
      !Number.isInteger(timeout) || timeout < 5 || timeout > 1440
        ? 'Session timeout must be a whole number between 5 and 1440'
        : undefined,
    passwordMinLength:
      !Number.isInteger(minLen) || minLen < 8 || minLen > 64
        ? 'Minimum password length must be between 8 and 64'
        : undefined,
  });
};
