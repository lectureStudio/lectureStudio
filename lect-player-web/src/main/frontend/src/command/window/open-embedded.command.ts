import { WindowCommand } from "./window.command";
import { WindowPresenter } from "../../api/presenter/window.presenter";
import { RecordingFileReader } from "../../io/recording-file-reader";
import { RawRecording, Recording } from "../../model/recording";
import { PdfJsDocument } from "../../model/pdf-js-document";
import { getDocument } from 'pdfjs-dist';
import { PDFDocumentProxy } from 'pdfjs-dist/types/display/api';

class OpenEmbeddedCommand implements WindowCommand {

	private readonly data: any;


	constructor(data: any) {
		this.data = data;
	}

	execute(presenter: WindowPresenter): Promise<void> {
		return new Promise((resolve, reject) => {
			const byteArray = Uint8Array.from(
				atob(this.data)
					.split('')
					.map(char => char.charCodeAt(0))
			);
	
			const file = new File([new Blob([byteArray])], "embedded", { lastModified: 1534584790000 });
	
			const recordingReader = new RecordingFileReader();
			const promise = recordingReader.read(file);
			promise.then((rawRecording: RawRecording) => {
				getDocument(rawRecording.document)
					.promise.then((pdf: PDFDocumentProxy) => {
						const doc = new PdfJsDocument(pdf);
	
						const recording = new Recording();
						recording.actions = rawRecording.actions;
						recording.audio = rawRecording.audio;
						recording.document = doc;
	
						presenter.setRecording(recording);
					},
					(reason: string) => {
						console.error(reason);
					});
			});
			promise.catch((error) => {
				console.error(error);
			});
		});
	}
}

export { OpenEmbeddedCommand };