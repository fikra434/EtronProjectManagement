import { Component, OnInit } from '@angular/core';
import { MatDialog , MatDialogConfig} from '@angular/material/dialog';
import { SignupComponent } from '../signup/signup.component';
import { ForgotPasswordComponent } from '../forgot-password/forgot-password.component';
import { LoginComponent } from '../login/login.component';
import { UserService } from '../services/user.service';
import { Router } from '@angular/router';
import { error } from 'console';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['../../assets/assets1/style.css', '../../assets/assets1/about.css']
})
export class HomeComponent implements OnInit {

  constructor(private dialog:MatDialog,
    private userServices:UserService,
    private router:Router) { }

  ngOnInit(): void {
    this.userServices.checkToken().subscribe((response:any)=>{
      this.router.navigate(['/cafe/router']);
    },(error:any)=>{
      console.log(error);
    })
  }

  handleSignupAction(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width="550px";
    this.dialog.open(SignupComponent,dialogConfig);

  }
  handleForgotPasswordAction(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width="550px";
    this.dialog.open(ForgotPasswordComponent,dialogConfig);

  } 
  handleLoginAction(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width="550px";
    this.dialog.open(LoginComponent,dialogConfig);
  }
}