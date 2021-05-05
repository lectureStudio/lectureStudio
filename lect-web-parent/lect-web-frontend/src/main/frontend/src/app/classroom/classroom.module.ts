import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { CoreModule } from '../core/core.module';
import { MaterialModule } from '../app.material.module';
import { ServiceAbsentComponent } from './service-absent/service-absent.component';
import { MessageFormComponent } from './message-form/message-form.component';
import { QuizFormComponent } from './quiz-form/quiz-form.component';
import { MultipleChoiceComponent, NumericComponent, SingleChoiceComponent } from './quiz-form/option';

@NgModule({
	imports: [
		CoreModule,
		CommonModule,
		FormsModule,
		ReactiveFormsModule,
		TranslateModule,
		MaterialModule,
	],
	exports: [
		MessageFormComponent,
		MultipleChoiceComponent,
		NumericComponent,
		QuizFormComponent,
		ServiceAbsentComponent,
		SingleChoiceComponent
	],
	declarations: [
		MessageFormComponent,
		MultipleChoiceComponent,
		NumericComponent,
		QuizFormComponent,
		ServiceAbsentComponent,
		SingleChoiceComponent
	]
})
export class ClassroomModule { }
