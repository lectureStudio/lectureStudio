import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Classroom } from '../../classroom/model';

@Injectable({
	providedIn: 'root'
})
export class AppStateService {

	public classroom: Classroom;

	public quizFormGroup: FormGroup;


	constructor() { }

}