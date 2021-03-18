import { Action } from "../action/action";

class RecordedPage {

	staticActions: Action[] = [];

	playbackActions: Action[] = [];

	pageNumber: number;

	timestamp: number;

}

export { RecordedPage };