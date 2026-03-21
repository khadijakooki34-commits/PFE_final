import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
  template: `
    <div class="auth-container d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <div class="card shadow-lg p-4" style="max-width: 400px; width: 100%;">
        <div class="text-center mb-4">
          <h2 class="fw-bold text-primary">{{ 'AUTH.RESET_TITLE' | translate }}</h2>
          <p class="text-muted">{{ 'AUTH.RESET_DESC' | translate }}</p>
        </div>

        <div *ngIf="validatingToken" class="text-center mb-3">
          <span class="spinner-border text-primary"></span>
          <p class="mt-2 text-muted">{{ 'AUTH.RESET_VALIDATING' | translate }}</p>
        </div>

        <div *ngIf="!validatingToken && !tokenValid && error" class="alert alert-danger mb-3 text-center">
          {{ error | translate }}
          <br><br>
          <a routerLink="/auth/forgot-password" class="btn btn-outline-danger btn-sm">{{ 'AUTH.RESET_REQUEST_NEW' | translate }}</a>
        </div>

        <div *ngIf="!validatingToken && successMessage" class="alert alert-success mt-3 text-center">
            {{ successMessage | translate }}
            <br><br>
            <a routerLink="/auth/login" class="btn btn-primary w-100">{{ 'AUTH.LOGIN_BTN' | translate }}</a>
        </div>

        <form *ngIf="!validatingToken && tokenValid && !successMessage" [formGroup]="resetForm" (ngSubmit)="onSubmit()">
          
          <div class="mb-3">
            <label class="form-label">{{ 'AUTH.RESET_TITLE' | translate }}</label>
            <input type="password" class="form-control" formControlName="newPassword" placeholder="******">
            <div *ngIf="resetForm.get('newPassword')?.touched && resetForm.get('newPassword')?.invalid" class="text-danger small mt-1">
              {{ 'AUTH.RESET_PASS_REQ' | translate }}
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label">{{ 'AUTH.RESET_CONFIRM' | translate }}</label>
            <input type="password" class="form-control" formControlName="confirmPassword" placeholder="******">
            <div *ngIf="resetForm.hasError('passwordsMismatch') && resetForm.get('confirmPassword')?.touched" class="text-danger small mt-1">
              {{ 'AUTH.RESET_PASS_MISMATCH' | translate }}
            </div>
          </div>

          <button type="submit" class="btn btn-primary w-100 mb-3" [disabled]="resetForm.invalid || loading">
            <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ 'AUTH.RESET_SUBMIT' | translate }}
          </button>
        </form>
      </div>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup;
  token: string | null = null;
  validatingToken = true;
  tokenValid = false;
  loading = false;
  error = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
    private cdr: ChangeDetectorRef
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.error = "AUTH.RESET_INVALID_LINK";
        this.validatingToken = false;
        return;
      }

      this.apiService.validateResetToken(this.token).subscribe({
        next: (isValid: boolean) => {
          this.validatingToken = false;
          if (isValid) {
            this.tokenValid = true;
          } else {
            this.error = "AUTH.RESET_EXPIRED";
          }
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          this.validatingToken = false;
          this.error = "AUTH.RESET_VALIDATION_ERROR";
          this.cdr.detectChanges();
        }
      });
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'passwordsMismatch': true };
  }

  onSubmit(): void {
    if (this.resetForm.invalid || !this.token) return;

    this.loading = true;
    this.error = '';

    const data = {
      token: this.token,
      newPassword: this.resetForm.value.newPassword
    };

    this.apiService.resetPassword(data).subscribe({
      next: (res: any) => {
        this.successMessage = res.message || "AUTH.RESET_SUCCESS";
        this.loading = false;
        this.cdr.detectChanges();
        
        // Redirect to login after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 2000);
      },
      error: (err: any) => {
        this.error = "AUTH.RESET_ERROR";
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
