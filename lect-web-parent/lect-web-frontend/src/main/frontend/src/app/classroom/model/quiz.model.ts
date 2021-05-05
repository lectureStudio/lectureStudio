export interface Quiz {

	type: QuizType;
	question: string;
	options: string[];

}

export enum QuizType {

	Multiple = 'MULTIPLE',
	Numeric = 'NUMERIC',
	Single = 'SINGLE'

}