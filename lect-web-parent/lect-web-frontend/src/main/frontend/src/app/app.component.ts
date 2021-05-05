import { Component } from '@angular/core';
import { Classroom, ClassroomServiceResponse, ClassroomServiceType, ServiceData, MessageServiceData } from './classroom/model';
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
				const serviceMap: any = classroom.services;
				const services = new Array<ServiceData>();

				for (var data of serviceMap.values()) {
					const type = Object.keys(data)[0];
					const content = JSON.parse(data[type]);

					var serviceData: ServiceData;

					switch (type) {
						case ClassroomServiceType.Quiz:
						case ClassroomServiceType.Message:
							serviceData = {
								_type: type,
								serviceId: 0
							};
							break;
					}

					services.push(Object.assign(serviceData, content));
				}

				classroom.services = services;

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
