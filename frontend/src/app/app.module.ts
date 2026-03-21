import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'; // Important for Material

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';
import { HomeComponent } from './home/home.component';
import { ProfileComponent } from './user/profile/profile.component';
import { ItineraryModule } from './itinerary/itinerary.module';
import { HelpCenterComponent } from './pages/help-center/help-center.component';
import { ContactComponent } from './pages/contact/contact.component';
import { TermsComponent } from './pages/terms/terms.component';
import { PrivacyPolicyComponent } from './pages/privacy-policy/privacy-policy.component';
import { TermsAndConditionsComponent } from './pages/terms-and-conditions/terms-and-conditions.component';

import { HttpClientModule, HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { TranslateModule, TranslateLoader, MissingTranslationHandler, MissingTranslationHandlerParams } from '@ngx-translate/core';
import { TranslateHttpLoader, provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { Observable } from 'rxjs';


export class CustomMissingTranslationHandler implements MissingTranslationHandler {
    handle(params: MissingTranslationHandlerParams) {
        console.warn(`[I18N Warning] Missing translation for key: ${params.key}`);
        const parts = params.key.split('.');
        const lastPart = parts[parts.length - 1];
        if (lastPart) {
            return lastPart.charAt(0).toUpperCase() + lastPart.slice(1).toLowerCase().replace(/_/g, ' ');
        }
        return params.key;
    }
}

import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

@NgModule({
    declarations: [
        AppComponent,
        HomeComponent,
        ProfileComponent,
        HelpCenterComponent,
        ContactComponent,
        TermsComponent,
        PrivacyPolicyComponent,
        TermsAndConditionsComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        CoreModule,
        TranslateModule.forRoot({
            defaultLanguage: 'en',
            loader: {
                provide: TranslateLoader,
                useClass: TranslateHttpLoader
            },
            missingTranslationHandler: {
                provide: MissingTranslationHandler,
                useClass: CustomMissingTranslationHandler
            },
            useDefaultLang: true
        }),
        SharedModule,
        ItineraryModule
    ],
    providers: [
        provideTranslateHttpLoader({
            prefix: 'assets/i18n/',
            suffix: '.json'
        }),
        provideCharts(withDefaultRegisterables())
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
