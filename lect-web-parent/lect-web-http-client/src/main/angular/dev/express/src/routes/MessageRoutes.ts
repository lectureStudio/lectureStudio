import { Router, Request, Response } from 'express';
import { ClassroomServiceResponse, ServiceResponseStatus } from '../../../../src/app/classroom/model';

const router: Router = Router();

router.post('/message/post', (req: Request, res: Response) => {
	console.log(req.body);

	const response: ClassroomServiceResponse = {
		statusCode: ServiceResponseStatus.Success,
		statusMessage: 'message.sent',
		data: []
	};

	res.status(200).send(response);
});

export const MessageServiceRouter: Router = router;