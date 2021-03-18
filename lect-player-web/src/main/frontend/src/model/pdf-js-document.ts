import { Page } from "./page";
import { SlideDocument } from "./document";
import { PDFDocumentProxy, PDFPageProxy } from 'pdfjs-dist/types/display/api';

class PdfJsDocument extends SlideDocument {

	private readonly document: PDFDocumentProxy;


	constructor(document: PDFDocumentProxy) {
		super();

		this.document = document;

		this.loadPages(document)
	}

	async getPageText(pageNumber: number): Promise<string> {
		const page = await this.document.getPage(pageNumber + 1);
		const content = await page.getTextContent();

		return content.items.map(function (s) { return s.str; }).join(' ');
	}

	getNativePage(pageNumber: number): Promise<PDFPageProxy> {
		return this.document.getPage(pageNumber + 1);
	}

	private loadPages(document: PDFDocumentProxy): void {
		this.pages = [];

		for (let i = 0; i < document.numPages; i++) {
			this.pages.push(new Page(this, i));
		}
	}
}

export { PdfJsDocument };