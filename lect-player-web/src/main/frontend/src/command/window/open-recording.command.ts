import { WindowCommand } from "./window.command";
import { WindowPresenter } from "../../api/presenter/window.presenter";
import { RecordingFileReader } from "../../io/recording-file-reader";
import { Recording, RawRecording } from "../../model/recording";
import { DocumentService } from "../../service/document.service";
import { SlideDocument } from "../../model/document";

class OpenRecordingCommand implements WindowCommand {

	private readonly file: File;


	constructor(file: File) {
		this.file = file;
	}

	execute(presenter: WindowPresenter): Promise<void> {
		return new Promise((resolve, reject) => {
			const recordingReader = new RecordingFileReader();
			const promise = recordingReader.read(this.file);
			promise.then((rawRecording: RawRecording) => {
				const docService = new DocumentService();
				docService.loadDocument(rawRecording.document)
					.then((doc: SlideDocument) => {
						const recording = new Recording();
						recording.actions = rawRecording.actions;
						recording.audio = rawRecording.audio;
						recording.document = doc;

						presenter.setRecording(recording);

						resolve();
					})
					.catch((reason: string) => {
						reject(reason);
					});
			});
			promise.catch((error) => {
				reject(error);
			});
		});
	}
}

export { OpenRecordingCommand };