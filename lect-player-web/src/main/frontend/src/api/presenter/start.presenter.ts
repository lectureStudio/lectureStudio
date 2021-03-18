import { Presenter } from "./presenter";
import { StartView } from "../view/start.view";
import { OpenRecordingCommand } from "../../command/window/open-recording.command";
import { ClassroomServiceClient } from "../../service/classroom.service";
import { Classroom } from "../../model/classroom";
import { WebSnackbar } from "../../component/snackbar/web-snackbar";
import { HttpResponse } from "../../utils/http-response";
import { OpenClassroomCommand } from "../../command/open-classroom.command";

class StartPresenter extends Presenter<StartView> {

	private classroomService: ClassroomServiceClient;


	initialize(): void {
		this.classroomService = new ClassroomServiceClient("http://192.168.0.73:80");

		this.view.setOnOpenRecording(this.onOpenRecording.bind(this));
		this.view.setOnOpenStream(this.onOpenStream.bind(this));
		this.view.setOnOpenClassroom(this.onOpenClassroom.bind(this));
	}

	private onOpenRecording(file: File): void {
		if (file) {
			this.execute(new OpenRecordingCommand(file))
				.catch(error => {
					const snackbar = new WebSnackbar();
					snackbar.setText(error);
					snackbar.show();

					console.error(error);
				});
		}
	}

	private onOpenStream(): void {
		this.classroomService.getClassrooms()
			.then((classrooms: Classroom[]) => {
				this.view.setClassrooms(classrooms);
			})
			.catch((error: HttpResponse<void>) => {
				const snackbar = new WebSnackbar();
				snackbar.setText("Failed to establish connection to the server");
				snackbar.show();

				console.error(error);
			});
	}

	private onOpenClassroom(classroom: Classroom): void {
		this.execute(new OpenClassroomCommand(classroom))
			.catch(error => {
				const snackbar = new WebSnackbar();
				snackbar.setText(error);
				snackbar.show();

				console.error(error);
			});
	}
}

export { StartPresenter };