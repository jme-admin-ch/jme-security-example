import {NgModule} from '@angular/core';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {TranslateModule} from '@ngx-translate/core';
import {
  OB_PAMS_CONFIGURATION,
  ObButtonModule,
  ObMasterLayoutConfig,
  ObMasterLayoutModule,
  provideObliqueConfiguration
} from '@oblique/oblique';
import {QdAuthModule} from '@quadrel-enterprise-ui/auth';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {appSetup, authConfig} from '../environments/environment';

@NgModule({
  bootstrap: [AppComponent],
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    QdAuthModule.forRoot(appSetup, authConfig),
    ObMasterLayoutModule,
    ObButtonModule,
    MatButtonModule,
    TranslateModule
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    // Authentication belongs to qd-auth. The example does not use the ePortal header backend.
    {provide: OB_PAMS_CONFIGURATION, useValue: {environment: null}},
    provideObliqueConfiguration({
      accessibilityStatement: {
        applicationName: 'JME Security Example',
        applicationOperator: 'Federal Office of Information Technology, Systems and Telecommunication FOITT',
        createdOn: new Date('2026-09-18'),
        conformity: 'none',
        contact: [{email: 'jeap-community@bit.admin.ch'}]
      }
    })
  ]
})
export class AppModule {
  constructor(config: ObMasterLayoutConfig) {
    config.homePageRoute = '/user';
    config.locale.locales = ['de', 'fr', 'it', 'en'];
    config.locale.defaultLanguage = 'de';
    Object.assign(config.header.serviceNavigation, {
      displayApplications: false,
      displayAuthentication: false,
      displayInfo: false,
      displayMessage: false,
      displayProfile: false,
      displayLanguages: true,
      eportalLanguageSynchronization: false,
      handleLogout: false
    });
  }
}
