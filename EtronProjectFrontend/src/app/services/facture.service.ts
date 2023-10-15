import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FactureService {

  url = environment.apiUrl;

  constructor(private httpClient:HttpClient) { }

  getFactures() {
    return this.httpClient.get(this.url + "/facture/");
  }

}
