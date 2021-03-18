import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root'
})
export class LoggerService {

	info(message: string) {
		console.info(message);
	}

	error(message: string) {
		console.error(message);
	}

	warn(message: string) {
		console.warn(message);
	}

}