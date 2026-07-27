import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class DadJokesService {

  private apiUrl = 'https://icanhazdadjoke.com/';

  constructor(private http: HttpClient) {}

  // Method to fetch a random joke
  getRandomJoke(): Observable<any> {
    const headers = new HttpHeaders({
      'Accept': 'application/json'
    });
    return this.http.get(this.apiUrl, { headers });
  }
}
