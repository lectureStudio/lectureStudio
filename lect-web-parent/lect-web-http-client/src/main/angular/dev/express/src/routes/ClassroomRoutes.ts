import { Router, Request, Response } from 'express';

import {
	Classroom,
	ClassroomServiceType,
	MessageServiceData,
	QuizServiceData,
	QuizType
}
from '../../../../src/app/classroom/model';


const quizService: QuizServiceData = {
	_type: ClassroomServiceType.Quiz,
	serviceId: -405495471489427140,
	quiz: {
		question: "Whats <b>wrong</b>?",
		type: QuizType.Multiple,
		options: ["nothing", "everything", "something"]
	}
};

const messageService: MessageServiceData = {
	_type: ClassroomServiceType.Message,
	serviceId: 8529181951635690000
};

const classroom: Classroom = {
	name: "Test Classroom",
	services: [ quizService, messageService ]
};


const router: Router = Router();

router.get('/classroom', (req: Request, res: Response) => {
	res.status(200).send(classroom);
});

router.get('/classroom/list', (req: Request, res: Response) => {
	const classrooms = [ classroom ];

	res.status(200).send(classrooms);
});

export const ClassroomServiceRouter: Router = router;