import { getDocument } from 'pdfjs-dist';
import { PDFDocumentProxy } from 'pdfjs-dist/types/display/api';
import { PdfJsDocument } from '../model/pdf-js-document';
import { SlideDocument } from '../model/document';

export class DocumentService {

	loadDocument(source: Uint8Array): Promise<SlideDocument> {
		return new Promise<PdfJsDocument>((resolve, reject) => {
			getDocument(source)
				.promise.then((pdf: PDFDocumentProxy) => {
					resolve(new PdfJsDocument(pdf));
				},
				(reason: string) => {
					reject(reason);
				});
		});
	}

}