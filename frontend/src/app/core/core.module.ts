import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';
import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { AuthService } from './services/auth.service';
import { AuthGuard } from './guards/auth.guard';
import { ApiService } from './services/api.service';
import { HttpLoggingInterceptor } from './interceptors/http-logging.interceptor';
import { LanguageInterceptor } from './interceptors/language.interceptor';

@NgModule({
    declarations: [],
    imports: [
        CommonModule,
        TranslateModule
    ],
    exports: [
        TranslateModule
    ],
    providers: [
        AuthService,
        ApiService,
        AuthGuard,
        provideHttpClient(withInterceptorsFromDi(), withFetch()),
        { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: HttpLoggingInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: LanguageInterceptor, multi: true }
    ]
})
export class CoreModule {
    constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
        if (parentModule) {
            throw new Error('CoreModule is already loaded. Import it in the AppModule only');
        }
    }
}
