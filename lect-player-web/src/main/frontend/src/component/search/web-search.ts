import { ViewElement } from "../../view/view-element";
import { WebViewElement } from "../../view/web-view-element";

@ViewElement({
	selector: "web-search",
	templateUrl: "web-search.html",
	styleUrls: ["web-search.css"]
})
class WebSearch extends WebViewElement {

	private searchClearButton: HTMLElement;

	private searchBackwardButton: HTMLElement;

	private searchForwardButton: HTMLElement;

	private searchResult: HTMLElement;

	private searchResultText: HTMLElement;

	private searchField: HTMLInputElement;

	private searchButton: HTMLElement;

	private _searchResult: SearchResult;

	private _searchFunction: (query: string) => Promise<SearchResult>;

	private _onSearchBackward: any;

	private _onSearchForward: any;


	constructor() {
		super();
	}

	connectedCallback() {
		this.searchField.addEventListener("keyup", (event) => {
			event.preventDefault();

			if (event.keyCode === 13) {
				this.search();
			}
		});

		this.searchClearButton.addEventListener("click", this.clearSearch.bind(this));
		this.searchBackwardButton.addEventListener("click", this.searchBackward.bind(this));
		this.searchForwardButton.addEventListener("click", this.searchForward.bind(this));
		this.searchButton.addEventListener("click", this.search.bind(this));
	}

	set searchFunction(func: (query: string) => Promise<SearchResult>) {
		this._searchFunction = func;
	}

	set onSearchBackward(func: (result: SearchResult) => void) {
		this._onSearchBackward = func;
	}

	set onSearchForward(func: (result: SearchResult) => void) {
		this._onSearchForward = func;
	}

	search() {
		if (this._searchFunction) {
			this._searchFunction(this.searchField.value).then((result: SearchResult) => {
				this._searchResult = result;

				if (this._searchResult) {
					// Show result.
					this.updateSearchResultView();

					// Go to the first found search result.
					this.searchForward();
				}
			});
		}
	}

	clearSearch() {
		this._searchResult = null;

		this.updateSearchResultView();
	}

	searchBackward() {
		if (this._searchResult && this._searchResult.backward()) {
			if (this._onSearchBackward) {
				this._onSearchBackward(this._searchResult);
			}

			this.updateSearchResultView();
		}
	}

	searchForward() {
		if (this._searchResult && this._searchResult.forward()) {
			if (this._onSearchForward) {
				this._onSearchForward(this._searchResult);
			}

			this.updateSearchResultView();
		}
	}

	updateSearchResultView() {
		if (this._searchResult && this._searchResult.getFound() > 0) {
			this.searchResultText.innerHTML = this._searchResult.getCurrentIndex() + " of " + this._searchResult.getFound();

			this.setElementVisibility(this.searchResult, true);
		}
		else {
			this.searchResultText.innerHTML = "0";

			this.setElementVisibility(this.searchResult, false);
		}
	}

	setElementVisibility(element: HTMLElement, visible: boolean) {
		if (visible) {
			element.classList.remove("slide-out");
			element.classList.add("slide-in");
		}
		else {
			element.classList.remove("slide-in");
			element.classList.add("slide-out");
		}
	}
}

class SearchResult {

	private readonly pageIndices: number[];

	private index: number;


	constructor(pageIndices: number[]) {
		this.pageIndices = pageIndices;
		this.index = 0;
	}

	getFound() {
		return this.pageIndices.length;
	}

	getCurrentIndex() {
		return this.index;
	}

	getCurrentPageIndex() {
		const index = this.index - 1 < 0 ? 0 : this.index - 1;
		return this.pageIndices[index];
	}

	forward() {
		if (this.index + 1 > this.pageIndices.length) {
			this.index = 1;
		}
		else {
			this.index++;
		}

		return true;
	}

	backward() {
		if (this.index - 1 < 1) {
			this.index = this.pageIndices.length;
		}
		else {
			this.index--;
		}

		return true;
	}
}

export { WebSearch, SearchResult };