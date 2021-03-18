import { RecordedPage } from "../model/recorded-page";
import { FileActionParser } from "../action/parser/file-action.parser";
import { ProgressiveDataView } from "../action/parser/progressive-data-view";
import { RawRecording } from "../model/recording";

class RecordingFileHeader {

	static readonly FILE_MARKER: number = 777014354;

	static readonly LENGTH: number = 48;

	version: number;

	duration: number;

	checksum: ArrayBuffer;

	actionLength: number;

	audioLength: number;

	documentLength: number;

}

class RecordingFileReader {

	async read(file: File): Promise<RawRecording> {
		let start = 0;
		let end = RecordingFileHeader.LENGTH;
		let fileHeader: RecordingFileHeader = null;

		const recording = new RawRecording();

		return this.readHeader(file)
			.then(header => {
				fileHeader = header;

				start = end;
				end += fileHeader.actionLength;

				return this.readActions(file, start, end);
			})
			.then(actions => {
				recording.actions = actions;

				start = end;
				end += fileHeader.documentLength;

				return this.readDocument(file, start, end);
			})
			.then(document => {
				recording.document = document;

				start = end;
				end += fileHeader.audioLength;

				return this.readAudio(file, start, end);
			})
			.then(audio => {
				recording.audio = audio;

				return recording;
			});
	}

	private readActions(file: File, start: number, end: number): Promise<RecordedPage[]> {
		const fileReader = new FileReader();
		const blob = file.slice(start, end);

		return new Promise((resolve, reject) => {
			fileReader.onloadend = (event) => {
				const target: any = event.target;

				if (target.error) {
					reject(target.error);
				}
				else {
					const actionParser = new FileActionParser();
					const actions = actionParser.parse(target.result);

					resolve(actions);
				}
			};
			fileReader.readAsArrayBuffer(blob);
		});
	}

	private readAudio(file: File, start: number, end: number): Blob {
		return file.slice(start, end);
	}

	private readDocument(file: File, start: number, end: number): Promise<Uint8Array> {
		const fileReader = new FileReader();
		const blob = file.slice(start, end);

		return new Promise((resolve, reject) => {
			fileReader.onloadend = (event) => {
				const target: any = event.target;

				if (target.error) {
					reject(target.error);
				}
				else {
					const documentData = new Uint8Array(target.result);

					resolve(documentData);
				}
			};
			fileReader.readAsArrayBuffer(blob);
		});
	}

	private readHeader(file: File): Promise<RecordingFileHeader> {
		const fileReader = new FileReader();
		const blob = file.slice(0, RecordingFileHeader.LENGTH);

		return new Promise((resolve, reject) => {
			fileReader.onloadend = (event) => {
				const target: any = event.target;

				if (target.error) {
					reject(target.error);
				}
				else {
					const dataView = new ProgressiveDataView(target.result);

					const marker = dataView.getInt32();

					if (marker !== RecordingFileHeader.FILE_MARKER) {
						reject("Invalid recording file header.");
						return;
					}

					const header = new RecordingFileHeader();
					header.version = dataView.getInt32();

					// Skip duration.
					dataView.skip(8);

					// Skip checksum.
					dataView.skip(20);

					header.actionLength = dataView.getInt32();
					header.documentLength = dataView.getInt32();
					header.audioLength = dataView.getInt32();

					resolve(header);
				}
			};
			fileReader.readAsArrayBuffer(blob);
		});
	}
}

export { RecordingFileHeader, RecordingFileReader };