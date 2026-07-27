import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, JsonPipe } from "@angular/common";
import { QdAuthenticationService, QdClaims } from "@quadrel-enterprise-ui/auth";
import { QdUiModule } from "@quadrel-enterprise-ui/framework";
import { Observable, forkJoin } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { appEnvironment } from "../../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from "@angular/material/button";
import { DadJokesService } from "../../shared/dad-jokes.service";


@Component({
  selector: 'app-user-overview',
  templateUrl: './claims.component.html',
  styleUrls: ['./claims.component.scss'],
  imports: [
    AsyncPipe,
    JsonPipe,
    QdUiModule,
    MatCardModule,
    MatButtonModule,
  ],
  standalone: true
})
export class ClaimsComponent implements OnInit {

  isAuthenticated$: Observable<boolean>;
  userClaims$: Observable<QdClaims>;
  result: any;
  claimsResult: any;
  userInfoResult: any;
  success = false;
  joke: string = '';

  constructor(
    private readonly authenticationService: QdAuthenticationService,
    private readonly http: HttpClient,
    private dadJokesService: DadJokesService
  ) {
    this.userClaims$ = this.authenticationService.claims$;
    this.isAuthenticated$ = this.authenticationService.isAuthenticated$;
  }

  ngOnInit(): void {
    console.log("### UserComponent called");

    // Using forkJoin to make both requests in parallel
    forkJoin({
      roles: this.http.get<any[]>(`${appEnvironment.BACKEND_SERVICE_API}api/v1/roles`).pipe(
        catchError(err => {
          console.error("Failed to fetch roles:", err);
          return [];
        })
      ),
      claims: this.http.get<any[]>(`${appEnvironment.BACKEND_SERVICE_API}api/v1/claims`).pipe(
        catchError(err => {
          console.error("Failed to fetch claims:", err);
          return [];
        })
      ),
      userInfo: this.http.get<any[]>(`${appEnvironment.BACKEND_SERVICE_API}api/current-user`).pipe(
        catchError(err => {
          console.error("Failed to fetch userInfo:", err);
          return [];
        })
      )
    }).subscribe({
      next: ({ roles, claims, userInfo}) => {
        this.result = roles;
        this.claimsResult = claims;
        this.userInfoResult = userInfo;
        this.success = true;
      },
      error: (err) => {
        console.error("An error occurred while fetching data:", err);
        this.success = false;
      }
    });

    this.isAuthenticated$.subscribe(value => {
      console.log('isAuthenticated$', value);
    });
  }

  makePublicRequest(): void {
    // Requests to other URLs do not get the token to avoid token leakage

    this.dadJokesService.getRandomJoke().subscribe({
      next: (response) => {
        this.joke = response.joke;
      },
      error: (error) => {
        console.error('Error fetching joke:', error);
        this.joke = 'Oops! Could not fetch a joke right now.';
      }
    });
  }

}
