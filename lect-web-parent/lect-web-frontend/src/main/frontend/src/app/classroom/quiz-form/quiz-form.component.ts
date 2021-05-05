import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { QuizFormHandler, getChoiceHandler } from './quiz-form-handler';
import { QuizService } from './service/quiz.service';
import { QuizAnswer } from './model/quiz-answer.model';
import { ClassroomServiceResponse, QuizServiceData, ServiceResponseStatus, ClassroomServiceType } from '../model';
import { AppStateService, LocaleService, NotificationService } from '../../core/service';

@Component({
	selector: 'quiz-form',
	templateUrl: './quiz-form.component.html',
	styleUrls: ['./quiz-form.component.scss']
})
export class QuizFormComponent implements OnInit {

	serviceData: QuizServiceData;

	form: FormGroup;

	formHandler: QuizFormHandler;


	constructor(
		private quizService: QuizService,
		private notifyService: NotificationService,
		private appStateService: AppStateService,
		private localeService: LocaleService)
	{
		const quizServiceData = appStateService.classroom.services.find(service => {
			return service._type === ClassroomServiceType.Quiz;
		});

		this.serviceData = <QuizServiceData> quizServiceData;
	}

	ngOnInit() {
		this.form = this.appStateService.quizFormGroup;

		if (!this.form) {
			this.formHandler = getChoiceHandler(this.serviceData.quiz.type);
			this.form = this.formHandler.createFormGroup(this.serviceData.quiz.options);
		}
	}

	onSubmit(): void {
		const result = this.formHandler.getInput(this.form);

		const answer: QuizAnswer = {
			serviceId: this.serviceData.serviceId,
			options: result
		};

		this.quizService.addQuizAnswer(answer).subscribe(
			(response: ClassroomServiceResponse) => {
				const message = this.localeService.translate(response.statusMessage);

				if (response.statusCode !== ServiceResponseStatus.Success) {
					this.notifyService.showErrorMessage(message);
				}
				else {
					this.form.disable();
					this.appStateService.quizFormGroup = this.form;
					this.notifyService.showMessage(message);
				}
			}
		);
	}
}
