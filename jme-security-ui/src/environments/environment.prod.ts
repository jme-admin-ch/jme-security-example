import {QdAppSetup, QdAuthConfigServerSide, QdLogLevel} from '@quadrel-enterprise-ui/auth';
import {QdAppEnvironment} from '@quadrel-enterprise-ui/framework';


export const appSetup: QdAppSetup = {
  production: true,
  serviceEndpoint: '/jme-security-scs/'
};

export const authConfig: QdAuthConfigServerSide = {
  configPathSegment: 'api/configuration',
  clientId: 'jme-security-ui',
  systemName: 'jme',
  logLevel: QdLogLevel.Debug,
  renewUserInfoAfterTokenRenew: true,
  silentRenew: true,
  silentRenewUrl: `${window.location.origin}/jme-security-scs/assets/auth/silent-renew.html`,
  useAutoLogin: true,
  redirectUrl: `${window.location.origin}/jme-security-scs/redirect`
};

export const appEnvironment: QdAppEnvironment = {
  production: appSetup.production,
  BACKEND_SERVICE_API: appSetup.serviceEndpoint,
  CONFIGURATION_PATH: 'api/configuration'
}
