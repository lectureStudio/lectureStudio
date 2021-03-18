import { Command } from "./command";
import { Classroom } from "../model/classroom";
import { ClassroomServiceClient } from "../service/classroom.service";
import { SlideDocument } from "../model/document";
import { WindowPresenter } from "../api/presenter/window.presenter";

class OpenClassroomCommand implements Command<WindowPresenter> {

	readonly classroom: Classroom;


	constructor(classroom: Classroom) {
		this.classroom = classroom;
	}

	execute(presenter: WindowPresenter): Promise<void> {
		return new Promise((resolve, reject) => {
			const classroomDocs = this.classroom.documents;

			if (!classroomDocs || classroomDocs.length < 1) {
				reject("Classroom has no documents available");
				return;
			}

			const service = new ClassroomServiceClient("http://192.168.0.73:80");
			service.getClassroomDocument(this.classroom, classroomDocs[0])
				.then((doc: SlideDocument) => {
					try {
						presenter.setStreamPlayer(this.classroom, doc);

						resolve();
					}
					catch (error) {
						reject(error);
					}
				})
				.catch(error => {
					reject(error);
				});
		});
	}

}

export { OpenClassroomCommand };