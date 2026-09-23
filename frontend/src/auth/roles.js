export const ROLES = { ADMIN: 'ADMIN', MANAGER: 'MANAGER', VIEWER: 'VIEWER' };

/** Roles allowed to create/edit campaigns, placements and place bids. */
export const EDIT_ROLES = [ROLES.ADMIN, ROLES.MANAGER];

export const hasRole = (user, roles) => !!user && roles.includes(user.role);
