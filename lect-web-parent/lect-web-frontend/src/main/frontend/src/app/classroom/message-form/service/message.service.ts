import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Message } from '../model/message.model';
import { LectureService } from '../../service';
import { ClassroomServiceResponse } from '../../model';

@Injectable({
	providedIn: 'root'
})
export class MessageService {

	constructor(private lectureService: LectureService) {

	}

	addMessage(message: Message): Observable<ClassroomServiceResponse> {
		return this.lectureService.post<ClassroomServiceResponse>('/api/message/post', message);
	}

}
