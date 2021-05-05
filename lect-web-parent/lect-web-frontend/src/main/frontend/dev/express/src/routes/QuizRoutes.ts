import { Router, Request, Response } from 'express';

import {
	ClassroomServiceResponse,
	ServiceResponseStatus
}
from '../../../../src/app/classroom/model';


const router: Router = Router();

router.post('/quiz/post', (req: Request, res: Response) => {
	const response: ClassroomServiceResponse = {
		statusCode: ServiceResponseStatus.Success,
		statusMessage: 'quiz.answer.sent',
		data: []
	};

	res.status(200).send(response);
});

export const QuizServiceRouter: Router = router;