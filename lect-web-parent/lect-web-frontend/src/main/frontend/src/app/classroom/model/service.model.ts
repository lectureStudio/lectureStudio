export interface ServiceData {

	_type: ClassroomServiceType;
	serviceId: number;

}

export enum ClassroomServiceType {

	Message = 'MessageService',

	Quiz = 'QuizService'

}
