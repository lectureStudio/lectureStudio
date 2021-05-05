import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app.routing.module';
import { MaterialModule } from './app.material.module';
import { CoreModule } from './core/core.module';
import { ClassroomModule } from './classroom/classroom.module';
import { LocaleTranslateLoader } from './core/service';

@NgModule({
	declarations: [
		AppComponent
	],
	imports: [
		BrowserModule,
		BrowserAnimationsModule,
		HttpClientModule,

		AppRoutingModule,
		CoreModule,
		ClassroomModule,
		MaterialModule,

		TranslateModule.forRoot({
			loader: {
				provide: TranslateLoader,
				useClass: LocaleTranslateLoader
			}
		})
	],
	bootstrap: [AppComponent]
})
export class AppModule {

	constructor() { }
}
