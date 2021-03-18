import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { MaterialModule } from '../app.material.module';
import { LoadingComponent } from './loading/loading.component';
import { NavComponent } from './nav/nav.component';
import { EscapeHtmlPipe } from './pipe/keep-html.pipe';

@NgModule({
	imports: [
		CommonModule,
		MaterialModule,
		RouterModule,
		TranslateModule
	],
	exports: [
		EscapeHtmlPipe,
		LoadingComponent,
		NavComponent,
	],
	declarations: [
		EscapeHtmlPipe,
		LoadingComponent,
		NavComponent,
	]
})
export class CoreModule {}