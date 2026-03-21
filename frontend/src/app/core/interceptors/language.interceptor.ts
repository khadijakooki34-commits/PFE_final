import { Injectable, Injector } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class LanguageInterceptor implements HttpInterceptor {
    private translate?: TranslateService;

    constructor(private readonly injector: Injector) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (!this.translate) {
            this.translate = this.injector.get(TranslateService);
        }
        const lang = this.translate?.currentLang || this.translate?.defaultLang || 'en';
        
        request = request.clone({
            setHeaders: {
                'Accept-Language': lang
            }
        });

        return next.handle(request);
    }
}
