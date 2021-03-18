import { RecordedPage } from "./recorded-page";
import { SlideDocument } from "./document";

class Recording {

	audio: Blob;

	document: SlideDocument;

	actions: RecordedPage[];

}

class RawRecording {

	audio: Blob;

	document: Uint8Array;

	actions: RecordedPage[];

}

export { Recording, RawRecording };