import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { SnackbarService } from '../services/snackbar.service';
import { MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { GlobalConstants } from '../shared/global-constants';
import {MatFormFieldModule} from '@angular/material/form-field';
@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss'],
})
export class SignupComponent implements OnInit {
  password = true;
  confirmPassword = true;
  signupForm: any = FormGroup;
  responseMessage: any;
  hide = true;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private userService: UserService,
    private snackbarService: SnackbarService,
    private dialogRef: MatDialogRef<SignupComponent>,
    private ngxService: NgxUiLoaderService
  ) {}

  ngOnInit(): void {
    this.signupForm = this.formBuilder.group({
      name: [null,[Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      prenom: [null,[Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      email: [null,[Validators.required, Validators.pattern(GlobalConstants.emailRegex)]],
      adresse: [null,[Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      role: [null,[Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      modeleVoiture: [null,[Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      contactnumber: [null,[Validators.required,Validators.pattern(GlobalConstants.contactNumberRegex)]],
      password: [null, [Validators.required]],
      confirmPassword: [null, [Validators.required]]
    })
  }

  validateSubmit() {
    if (
      this.signupForm.controls['password'].value != this.signupForm.controls['confirmPassword'].value) {
      return true;
    } else {
      return false;
    }
  }

  handleSubmit() {
    this.ngxService.start();
    var formData = this.signupForm.value;
    var data = {
      name: formData.name,
      prenom: formData.prenom,
      adresse: formData.adresse,
      modeleVoiture: formData.modeleVoiture,
      role: formData.role,
      email: formData.email,
      contactnumber: formData.contactnumber,
      password: formData.password,
    }
    this.userService.signUp(data).subscribe((response: any) => {
        this.ngxService.stop();
        this.dialogRef.close();
        this.responseMessage = response?.message;
        this.snackbarService.openSnackBar(this.responseMessage, "");
        this.router.navigate(['/']);
      },
      (error) => {
        this.ngxService.stop();
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage,GlobalConstants.error);
      })
  }
}
