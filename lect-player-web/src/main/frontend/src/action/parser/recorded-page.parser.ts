import { ProgressiveDataView } from "./progressive-data-view";
import { ActionParser } from "./action.parser";
import { RecordedPage } from "../../model/recorded-page";

class RecordedPageParser {

	parse(dataView: ProgressiveDataView): RecordedPage {
		const recordedPage = new RecordedPage();
		
		const number = dataView.getInt32();
		const timestamp = dataView.getInt32();

		recordedPage.pageNumber = number;
		recordedPage.timestamp = timestamp;

		const staticActionSize = dataView.getInt32();

		if (staticActionSize > 0) {
			const end = dataView.byteOffset + staticActionSize;

			while (dataView.byteOffset < end) {
				const length = dataView.getInt32();
				const type = dataView.getInt8();
				const timestamp = dataView.getInt32();

				const action = ActionParser.parse(dataView, type, length);
				if (action) {
					action.timestamp = timestamp;

					recordedPage.staticActions.push(action);
				}
			}
		}

		const playbackActionSize = dataView.getInt32();
		if (playbackActionSize > 0) {
			const end = dataView.byteOffset + playbackActionSize;

			while (dataView.byteOffset < end) {
				const length = dataView.getInt32();
				const type = dataView.getInt8();
				const timestamp = dataView.getInt32();

				const action = ActionParser.parse(dataView, type, length);
				if (action) {
					action.timestamp = timestamp;

					recordedPage.playbackActions.push(action);
				}
			}
		}

		return recordedPage;
	}

}

export { RecordedPageParser };