export interface ClassroomServiceResponse {

	statusCode: ServiceResponseStatus;

	statusMessage: string;

	data?: any;

}

export enum ServiceResponseStatus {

	Success,

	Error,

	DataError

}