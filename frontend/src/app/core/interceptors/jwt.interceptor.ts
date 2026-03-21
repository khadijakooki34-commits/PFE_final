import { Injectable, Injector } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    private authService?: AuthService;

    constructor(private readonly injector: Injector) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (!this.authService) {
            this.authService = this.injector.get(AuthService);
        }
        const token = this.authService?.getToken();
        const isAuthUrl = request.url.includes('/api/auth');

        // Only add token if it exists and we're NOT calling an auth endpoint 
        // (Login/Register don't need the Bearer token usually, though it doesn't hurt, 
        // but it can cause 401s if an expired token is sent during login)
        if (token && !isAuthUrl) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }

        return next.handle(request);
    }
}
