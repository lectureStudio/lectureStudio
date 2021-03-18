import { Observer } from "../../utils/observable";
import { ToolbarView } from "../../api/view/toolbar.view";
import { ViewElement } from "../view-element";
import { WebSelect } from "../../component/select/web-select";
import { WebSearch, SearchResult } from "../../component/search/web-search";
import { WebViewElement } from "../web-view-element";

@ViewElement({
	selector: "toolbar-view",
	templateUrl: "web-toolbar.view.html",
	styleUrls: ["web-toolbar.view.css"]
})
class WebToolbarView extends WebViewElement implements ToolbarView {

	private openRecordingButton: HTMLElement;

	private sidebarPosSelect: WebSelect;

	private search: WebSearch;


	constructor() {
		super();
	}

	setSidebarPosition(): void {
		throw new Error("Method not implemented.");
	}

	setOnSiderbarPosition(observer: Observer<string>): void {
		this.sidebarPosSelect.valueProperty.subscribe(observer);
	}

	setOnSearch(func: (query: string) => Promise<SearchResult>): void {
		this.search.searchFunction = func;
	}

	setOnSearchPrevious(func: (result: SearchResult) => void): void {
		this.search.onSearchBackward = func;
	}

	setOnSearchNext(func: (result: SearchResult) => void): void {
		this.search.onSearchForward = func;
	}

	setOnOpenRecording(observer: Observer<File>): void {
		/*
		this.openRecordingButton.addEventListener("change", (event: Event) => {
			var input = <HTMLInputElement>event.target;
			const file = input.files[0];

			observer(file);
		}, false);
		*/
	}
}

export { WebToolbarView };