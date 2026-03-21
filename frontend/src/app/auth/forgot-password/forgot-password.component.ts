import { Component }  from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule }  from '@angular/forms';
import { RouterModule }  from '@angular/router';
import { ApiService }  from '../../core/services/api.service';
import { CommonModule }  from '@angular/common';
import { TranslateModule }  from '@ngx-translate/core';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
    template: `
    <div class="auth-container d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <div class="card shadow-lg p-4" style="max-width: 400px; width: 100%;">
        <div class="text-center mb-4">
          <h2 class="fw-bold text-primary">{{ 'AUTH.FORGOT_TITLE' | translate }}</h2>
          <p class="text-muted">{{ 'AUTH.FORGOT_DESC' | translate }}</p>
        </div>

        <form [formGroup]="forgotForm" (ngSubmit)="onSubmit()">
          <div class="mb-3">
            <label class="form-label">{{ 'AUTH.EMAIL' | translate }}</label>
            <input type="email" class="form-control" formControlName="email" [placeholder]="'AUTH.EMAIL_HINT' | translate">
            <div *ngIf="forgotForm.get('email')?.touched && forgotForm.get('email')?.invalid" class="text-danger small">
              {{ 'AUTH.FORGOT_EMAIL_INV' | translate }}
            </div>
          </div>

          <div *ngIf="message" class="alert alert-success small mb-3">
            {{ message | translate }}
          </div>
          <div *ngIf="error" class="alert alert-danger small mb-3">
            {{ error | translate }}
          </div>

          <button type="submit" class="btn btn-primary w-100 mb-3" [disabled]="forgotForm.invalid || loading">
            <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ 'AUTH.FORGOT_SUBMIT' | translate }}
          </button>

          <div class="text-center">
            <a routerLink="/auth/login" class="text-decoration-none">{{ 'AUTH.TWO_FACTOR.BACK' | translate }}</a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class ForgotPasswordComponent {
    forgotForm: FormGroup;
    loading = false;
    message = '';
    error = '';

    constructor(private fb: FormBuilder, private apiService: ApiService) {
        this.forgotForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]]
        });
    }

    onSubmit() {
        if (this.forgotForm.invalid) return;

        this.loading = true;
        this.message = '';
        this.error = '';

        this.apiService.forgotPassword(this.forgotForm.value.email).subscribe({
            next: (res: any) => {
                this.message = (res && res.message) ? res.message : 'AUTH.FORGOT_SUCCESS';
                this.loading = false;
                this.forgotForm.reset();
            },
            error: (err: any) => {
                this.error = (err.error && err.error.message) ? err.error.message : 'AUTH.FORGOT_ERROR';
                this.loading = false;
            }
        });
    }
}
