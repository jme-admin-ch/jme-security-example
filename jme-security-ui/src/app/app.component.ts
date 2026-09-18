import {Component} from '@angular/core';
import {QdAuthenticationService} from '@quadrel-enterprise-ui/auth';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false
})
export class AppComponent {
  readonly isAuthenticated$: Observable<boolean>;

  constructor(private readonly authenticationService: QdAuthenticationService) {
    this.isAuthenticated$ = authenticationService.isAuthenticated$;
  }

  login(): void {
    this.authenticationService.login();
  }

  logout(): void {
    this.authenticationService.logout();
  }
}
