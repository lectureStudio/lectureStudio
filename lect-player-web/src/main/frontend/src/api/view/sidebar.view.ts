import { View } from "./view";
import { SlideDocument } from "../../model/document";
import { Observer } from "../../utils/observable";

interface SidebarView extends View {

	setDocument(doc: SlideDocument): void;

	setSelectedPageIndex(lastIndex: number, newIndex: number): void;

	setOnPageSelect(observer: Observer<number>): void;

}

export { SidebarView };