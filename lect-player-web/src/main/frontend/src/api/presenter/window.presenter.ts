import { Presenter } from "./presenter";
import { Recording } from "../../model/recording";
import { WindowView } from "../view/window.view";
import { PlaybackPresenter } from "./playback.presenter";
import { WebPlaybackView, WebStartView, WebStreamPlaybackView } from "../../view";
import { WebSnackbar } from "../../component/snackbar/web-snackbar";
import { StartPresenter } from "./start.presenter";
import { View } from "../view/view";
import { FullscreenCommand } from "../../command/fullscreen.command";
import { OpenRecordingCommand } from "../../command/window/open-recording.command";
import { OpenClassroomCommand } from "../../command/open-classroom.command";
import { StreamPlaybackPresenter } from "./stream-playback.presenter";
import { Classroom } from "../../model/classroom";
import { SlideDocument } from "../../model/document";
import { HttpRequest } from "../../utils/http-request";

class WindowPresenter extends Presenter<WindowView> {

	initialize(): void {
		this.addCommandExecuter(FullscreenCommand.name, this.executeFullscreen);
		this.addCommandExecuter(OpenRecordingCommand.name, this.executeOpenRecording);
		this.addCommandExecuter(OpenClassroomCommand.name, this.executeOpenClassroom);

		const startView = new WebStartView();
		const startPresenter = new StartPresenter(startView, this);

		this.setContent(startView, startPresenter);
	}

	openRecording(filePath: string): void {
		new HttpRequest().setResponseType("arraybuffer").get(filePath)
			.then((data: ArrayBuffer) => {
				if (data) {
					this.execute(new OpenRecordingCommand(new File([ data ], filePath)))
						.catch(error => {
							const snackbar = new WebSnackbar();
							snackbar.setText(error);
							snackbar.show();
		
							console.error(error);
						});
				}
			})
			.catch((reason: any) => {
				console.error(reason);
			});
	}

	setRecording(recording: Recording): void {
		const playbackView = new WebPlaybackView();
		const playbackPresenter = new PlaybackPresenter(playbackView, this, recording);

		this.setContent(playbackView, playbackPresenter);
	}

	setStreamPlayer(classroom: Classroom, doc: SlideDocument): void {
		const playbackView = new WebStreamPlaybackView();
		const playbackPresenter = new StreamPlaybackPresenter(playbackView, this, classroom, doc);

		this.setContent(playbackView, playbackPresenter);
	}

	private executeFullscreen(command: FullscreenCommand): Promise<void> {
		return command.execute();
	}

	private executeOpenRecording(command: OpenRecordingCommand): Promise<void> {
		return command.execute(this);
	}

	private executeOpenClassroom(command: OpenClassroomCommand): Promise<void> {
		return command.execute(this);
	}

	private setContent(view: View, presenter: Presenter<View>): void {
		this.view.setView(view);

		presenter.initialize();

		// Release resources of previous view.
		this.destroyChildren();
		this.addChild(presenter);
	}
}

export { WindowPresenter };