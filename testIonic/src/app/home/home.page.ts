import { Component } from '@angular/core';

declare var cordova;

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {

  constructor() {}

  toggleAirplane(){
    const airplane = cordova.plugins.Airplane;
    airplane.coolMethod((val) => {
        console.log(val);
    },
    (err) => {
        console.log(err);
    })
  }

}
