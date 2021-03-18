export interface ServiceData {

	_type: ClassroomServiceType;
	serviceId: number;

}

export enum ClassroomServiceType {

	Message = 'message',

	Quiz = 'quiz'

}