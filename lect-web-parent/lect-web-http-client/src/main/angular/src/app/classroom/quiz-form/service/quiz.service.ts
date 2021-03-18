import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { QuizAnswer } from '../model/quiz-answer.model';
import { LectureService } from '../../service';
import { ClassroomServiceResponse } from '../../model';

@Injectable({
	providedIn: 'root'
})
export class QuizService {

	constructor(private lectureService: LectureService) {

	}

	addQuizAnswer(answer: QuizAnswer): Observable<ClassroomServiceResponse> {
		return this.lectureService.post<ClassroomServiceResponse>('/bcast/ws/quiz/post', answer);
	}

}