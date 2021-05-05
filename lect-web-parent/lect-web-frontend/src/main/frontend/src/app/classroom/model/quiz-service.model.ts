import { ServiceData } from './service.model';
import { Quiz } from './quiz.model';

export interface QuizServiceData extends ServiceData {

	quiz: Quiz;

}