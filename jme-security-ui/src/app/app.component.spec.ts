import {CommonModule} from '@angular/common';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {TranslateModule} from '@ngx-translate/core';
import {QdAuthenticationService} from '@quadrel-enterprise-ui/auth';
import {BehaviorSubject} from 'rxjs';
import {AppComponent} from './app.component';

describe('authentication controls', () => {
  let authenticated: BehaviorSubject<boolean>;
  let auth: {isAuthenticated$: BehaviorSubject<boolean>; login: jest.Mock; logout: jest.Mock};

  beforeEach(async () => {
    authenticated = new BehaviorSubject(false);
    auth = {isAuthenticated$: authenticated, login: jest.fn(), logout: jest.fn()};
    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      imports: [CommonModule, TranslateModule.forRoot()],
      providers: [{provide: QdAuthenticationService, useValue: auth}],
      // Keep the real authentication controls; the Oblique layout is outside this test's scope.
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  it('starts login through Quadrel when the anonymous user clicks login', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.textContent).toContain('i18n.auth.login');
    button.click();
    expect(auth.login).toHaveBeenCalledTimes(1);
    expect(auth.logout).not.toHaveBeenCalled();
  });

  it('reacts to session changes and delegates logout to Quadrel', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    authenticated.next(true);
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.textContent).toContain('i18n.auth.logout');
    button.click();
    expect(auth.logout).toHaveBeenCalledTimes(1);
    authenticated.next(false);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button').textContent).toContain('i18n.auth.login');
  });
});
