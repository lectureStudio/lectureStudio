import { Observer } from "../../utils/observable";
import { View } from "./view";
import { SearchResult } from "../../component/search/web-search";

interface ToolbarView extends View {

	setSidebarPosition(): void;

	setOnSiderbarPosition(observer: Observer<string>): void;

	setOnSearch(func: (query: string) => Promise<SearchResult>): void;

	setOnSearchPrevious(func: (result: SearchResult) => void): void;

	setOnSearchNext(func: (result: SearchResult) => void): void;

	setOnOpenRecording(observer: Observer<File>): void;

}

export { ToolbarView };