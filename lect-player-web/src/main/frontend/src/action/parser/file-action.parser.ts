import { ProgressiveDataView } from "./progressive-data-view";
import { RecordedPageParser } from "./recorded-page.parser";
import { RecordedPage } from "../../model/recorded-page";

class FileActionParser {

	parse(buffer: ArrayBuffer): RecordedPage[] {
		const dataView = new ProgressiveDataView(buffer);
		const bufferLength = buffer.byteLength;

		const recordedPages: RecordedPage[] = [];

		while (dataView.byteOffset < bufferLength) {
			const entryLength = dataView.getInt32();

			const pageParser = new RecordedPageParser();
			const recordedPage = pageParser.parse(dataView);

			if (recordedPage) {
				recordedPages.push(recordedPage);
			}
		}

		return recordedPages;
	}

}

export { FileActionParser };