import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LectureService } from './lecture.service';
import { LoggerService } from '../../core/service';
import { Classroom, ClassroomServiceResponse, ServiceResponseStatus } from '../model';

@Injectable({
	providedIn: 'root'
})
export class ClassroomService {

	constructor(private lectureService: LectureService, private loggerService: LoggerService) {

	}

	getClassroom(): Observable<Classroom | ClassroomServiceResponse> {
		return this.lectureService.get<Classroom>('/api/classroom')
			.pipe(catchError(this.handleError())
			);
	}

	getClassrooms(): Observable<Classroom[] | ClassroomServiceResponse> {
		return this.lectureService.get<Classroom[]>('/api/classroom/list')
			.pipe(catchError(this.handleError())
			);
	}

	protected handleError() {
		return (error: HttpErrorResponse): Observable<ClassroomServiceResponse> => {
			const statusCode = error.status;

			if (statusCode === 0) {
				const response: ClassroomServiceResponse = {
					statusCode: ServiceResponseStatus.Error,
					statusMessage: 'connection.error'
				};

				return throwError(response);
			}
			else {
				const url = error.url;
				const statusText = error.statusText;
				let errorBody: ClassroomServiceResponse = error.error;

				if (!this.lectureService.isServiceResponse(error)) {
					errorBody = {
						statusCode: ServiceResponseStatus.Error,
						statusMessage: 'connection.' + statusCode
					};
				}

				this.loggerService.error(`${url} failed: ${statusText} [${statusCode}]`);

				return throwError(errorBody);
			}
		};
	}

}
