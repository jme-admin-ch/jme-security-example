import {QdRoleFilter} from '@quadrel-enterprise-ui/auth';
import {authConfig} from '../../environments/environment';

export const readQdRoleFilter: QdRoleFilter = {
  system: authConfig.systemName,
  resource: 'partner',
  operation: 'read'
};
