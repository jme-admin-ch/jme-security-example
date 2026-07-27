import {Component, OnInit} from '@angular/core';
import {QdDialogAuthSessionEndService, QdShellConfig, QdShellModule} from '@quadrel-enterprise-ui/framework';
import {QdAuthenticationService} from '@quadrel-enterprise-ui/auth';
import {RouterModule} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false
})
export class AppComponent implements OnInit {

  qdShellConfig: QdShellConfig = {
    title: {
      i18n: 'i18n.application.title'
    },
    hasSearch: false,
    isInternal: true,
    headerWidget: {
      isDisabled: true
    }
  };


  constructor(
    private readonly qdAuthenticationService: QdAuthenticationService,
    private readonly authSupport: QdDialogAuthSessionEndService,
    private readonly translateService: TranslateService
  ) {
  }

  ngOnInit(): void {
    const browserLanguage = this.translateService.getBrowserLang();
    const language = browserLanguage && ['de', 'en', 'fr', 'it'].includes(browserLanguage)
      ? browserLanguage
      : 'de';

    this.translateService.setFallbackLang('de');
    this.translateService.use(language);

    // Register the logout handler for the authentication service
    this.qdAuthenticationService.registerBeforeSessionLogoutHandler(this.authSupport.getLogoutHandler());
  }
}
