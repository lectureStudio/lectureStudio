import { Component } from '@angular/core';
import { Classroom, ClassroomServiceResponse } from './classroom/model';
import { ClassroomService } from './classroom/service';
import { AppStateService, LocaleService } from './core/service';
import { LoadingState, LoadingStateType } from './core/model';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.scss']
})
export class AppComponent {

	state: LoadingState;

	classroom: Classroom;


	constructor(
		private localeService: LocaleService,
		private classroomService: ClassroomService,
		private appStateService: AppStateService)
	{
		this.loadClassroom();
	}

	private loadClassroom() {
		this.state = {
			message: 'loading',
			type: LoadingStateType.Loading
		};

		this.classroomService.getClassroom().subscribe(
			(classroom: Classroom) => {
				this.appStateService.classroom = classroom;
				this.classroom = classroom;
			},
			(errorResponse: ClassroomServiceResponse) => {
				this.state.type = LoadingStateType.Error;
				this.state.message = errorResponse.statusMessage;
			}
		);
	}

	private onRetry() {
		this.loadClassroom();
	}
}