import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { SlideShape } from "../model/shape/slide.shape";
import { PDFPageProxy } from "pdfjs-dist/types/display/api";

class SlideRenderer implements ShapeRenderer {

	private readonly backCanvas = document.createElement('canvas');

	private renderTask: any;


	async render(context: CanvasRenderingContext2D, shape: SlideShape, dirtyRegion: Rectangle): Promise<CanvasImageSource> {
		if (this.renderTask) {
			return null;
		}

		const page = shape.getPage();
		const pageProxy: PDFPageProxy = await page.getNativePage();

		const scaleX = 1.0 / shape.bounds.width;
		const scaleTx = dirtyRegion.width * scaleX;

		const tx = shape.bounds.x * scaleTx;
		const ty = shape.bounds.y * scaleTx;

		const width = pageProxy.view[2] - pageProxy.view[0];
		const scale = scaleX * (dirtyRegion.width / width);
		const viewport: any = pageProxy.getViewport({
			scale: scale,
			dontFlip: false
		});

		viewport.transform[4] -= tx;
		viewport.transform[5] -= ty;

		this.backCanvas.width = dirtyRegion.width;
		this.backCanvas.height = dirtyRegion.height;

		this.renderTask = pageProxy.render({
			canvasContext: this.backCanvas.getContext('2d'),
			viewport: viewport,
		});

		return this.renderTask.promise.then(() => {
			context.canvas.width = this.backCanvas.width;
			context.canvas.height = this.backCanvas.height;
			context.drawImage(this.backCanvas, 0, 0);

			this.renderTask = null;

			return this.backCanvas;
		}, (reason: string) => {
			//console.error(reason);
		});
	}
}

export { SlideRenderer };