export class LoadingState {

	message: string;

	type: LoadingStateType;

}

export enum LoadingStateType {

	Loading,
	Error

}