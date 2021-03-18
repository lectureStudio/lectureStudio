import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { ClassroomServiceResponse } from '../model';
import { LoggerService, NotificationService } from '../../core/service';
import { ServiceAbsentComponent } from '../service-absent/service-absent.component';

@Injectable({
	providedIn: 'root'
})
export class LectureService {

	httpOptions = {
		headers: new HttpHeaders({
			'Content-Type': 'application/json'
		}),
		withCredentials: true
	};


	constructor(
		private router: Router,
		private http: HttpClient,
		private notifyService: NotificationService,
		private loggerService: LoggerService)
	{
	}

	public get<T>(url: string) {
		return this.http.get<T>(url, this.httpOptions);
	}

	public post<T>(url: string, body: any) {
		const jsonPayload = JSON.stringify(body);

		return this.http.post<T>(url, jsonPayload, this.httpOptions)
			.pipe(
				catchError(this.handleError())
			);
	}

	public isServiceResponse(response: any): response is ClassroomServiceResponse {
		const hasStatusCode = (<ClassroomServiceResponse>response).statusCode !== undefined;
		const hasStatusMessage = (<ClassroomServiceResponse>response).statusMessage !== undefined;

		return hasStatusCode && hasStatusMessage;
	}

	protected handleError() {
		return (error: HttpErrorResponse): Observable<ClassroomServiceResponse> => {
			const statusCode = error.status;

			if (statusCode === 0) {
				this.notifyService.showDialog(ServiceAbsentComponent).subscribe(_ => {
					this.redirectToHome();
				});

				return throwError(null);
			}
			else {
				const url = error.url;
				const statusText = error.statusText;
				const errorBody = error.error;

				if (!this.isServiceResponse(errorBody)) {
					this.loggerService.error(`${url} [${statusCode}]: ${statusText}`);

					this.notifyService.showDialog(ServiceAbsentComponent).subscribe(_ => {
						this.redirectToHome();
					});

					return throwError(null);
				}

				const response = errorBody as ClassroomServiceResponse;

				this.loggerService.error(`${url}: [${response.statusCode}] ${response.statusMessage}`);

				return Observable.of(response);
			}
		};
	}

	private redirectToHome() {
		window.location.reload();
	}

}