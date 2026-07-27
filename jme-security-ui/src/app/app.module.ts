import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {QdUiModule} from '@quadrel-enterprise-ui/framework';
import {StoreModule} from '@ngrx/store';
import {QdAuthModule} from '@quadrel-enterprise-ui/auth';
import {appEnvironment, appSetup, authConfig} from '../environments/environment';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ReactiveFormsModule} from '@angular/forms';
import {qdLanguageLoader} from "@quadrel-enterprise-ui/language";
import {TranslateModule} from '@ngx-translate/core';

@NgModule({ bootstrap: [AppComponent],
    declarations: [AppComponent],
    exports: [QdAuthModule], imports: [BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        StoreModule.forRoot({}),
        QdAuthModule.forRoot(appSetup, authConfig),
        QdUiModule.forRoot(appEnvironment),
        TranslateModule.forRoot(qdLanguageLoader()),
    ] })
export class AppModule {}
