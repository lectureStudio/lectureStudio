import { Page } from "../model/page";
import { Dimension } from "../geometry/dimension";
import { renderTextLayer } from "pdfjs-dist";

const pdfViever = require("pdfjs-dist/web/pdf_viewer");

class TextLayerSurface {

	private readonly root: HTMLElement;

	private size: Dimension;


	constructor(root: HTMLElement) {
		this.root = root;
	}

	clear(): void {
		while (this.root.firstChild) {
			this.root.removeChild(this.root.firstChild);
		}
	}

	async render(page: Page) {
		this.clear();

		const pageProxy = await page.getNativePage();
		const textContent = await pageProxy.getTextContent();

		const slideShape = page.getSlideShape();
		const slideBounds = slideShape.bounds;

		const scaleX = 1.0 / slideShape.bounds.width;
		const scaleTx = this.size.width * scaleX;

		const tx = slideShape.bounds.x * scaleTx;
		const ty = slideShape.bounds.y * scaleTx;

		const width = pageProxy.view[2] - pageProxy.view[0];
		const scale = scaleX * (this.size.width / width);
		const viewport: any = pageProxy.getViewport({
			scale: scale,
			dontFlip: false
		});

		viewport.transform[4] -= tx;
		viewport.transform[5] -= ty;

		renderTextLayer({
			textContent: textContent,
			container: this.root,
			viewport: viewport,
			enhanceTextSelection: true,
			textDivs: []
		})
		.promise.catch((reason: any) => {
			console.error(reason);
		});

		const linkService = new pdfViever.SimpleLinkService();
		// Open links in new tab.
		linkService.externalLinkTarget = 2;

		const annotationLayer = new pdfViever.AnnotationLayerBuilder({
			pageDiv: this.root,
			pdfPage: pageProxy,
			linkService: linkService
		});
		annotationLayer.render(viewport);
	}

	getSize(): Dimension {
		return this.size;
	}

	setSize(width: number, height: number): void {
		this.size = new Dimension(width, height);

		this.root.style.width = width + "px";
		this.root.style.height = height + "px";
	}
}

export { TextLayerSurface };