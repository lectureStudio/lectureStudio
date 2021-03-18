import { Page } from "./page";

abstract class SlideDocument {

	protected pages: Page[];


	abstract getNativePage(pageNumber: number): any;

	abstract async getPageText(pageNumber: number): Promise<string>;


	constructor() {
		
	}

	getPageCount(): number {
		return this.pages.length;
	}

	getPage(pageNumber: number): Page {
		if (pageNumber < 0 || pageNumber > this.pages.length - 1) {
			throw new Error(`Page number ${pageNumber} out of bounds.`);
		}
		return this.pages[pageNumber];
	}
}

export { SlideDocument };