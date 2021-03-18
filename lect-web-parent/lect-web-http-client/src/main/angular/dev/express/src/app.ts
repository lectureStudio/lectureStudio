import * as express from "express";
import * as bodyParser from "body-parser";
import * as cors from "cors";
import { ClassroomServiceRouter, MessageServiceRouter, QuizServiceRouter } from './routes';

class ServiceServer {

	public app: express.Application;

	readonly port = 4000;


	constructor() {
		this.app = express();
		this.config();

		this.app.listen(this.port, () => {
			console.log('Server started: http://localhost:' + this.port);
		});

		this.app.use('/bcast/ws/', ClassroomServiceRouter);
		this.app.use('/bcast/ws/', MessageServiceRouter);
		this.app.use('/bcast/ws/', QuizServiceRouter);
	}

	private config(): void {
		const corsOptions = {
			origin: 'http://localhost:4200',
			credentials: true
		};

		this.app.use(bodyParser.json());
		this.app.use(bodyParser.urlencoded({ extended: false }));
		this.app.use(cors(corsOptions));
	}

}

export default new ServiceServer().app;