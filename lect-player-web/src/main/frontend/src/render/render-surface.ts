import { Shape } from "../model/shape/shape";
import { ShapeRenderer } from "./shape.renderer";
import { Dimension } from "../geometry/dimension";
import { Rectangle } from "../geometry/rectangle";
import { SizeEvent } from "../event/size-event";
import { TypedEvent, Listener, Disposable } from "../utils/event-listener";
import { Transform } from "../geometry/transform";

class RenderSurface {

	protected readonly canvas: HTMLCanvasElement;

	protected readonly canvasContext: CanvasRenderingContext2D;

	protected readonly renderers: Map<String, ShapeRenderer>;

	private readonly sizeEvent = new TypedEvent<SizeEvent>();

	private readonly transform: Transform;

	private size: Dimension;


	constructor(canvas: HTMLCanvasElement) {
		this.canvas = canvas;
		this.canvasContext = canvas.getContext("2d");
		this.renderers = new Map();
		this.transform = new Transform();
	}

	clear(): void {
		this.canvasContext.clearRect(0, 0, this.canvas.width, this.canvas.height);
	}

	registerRenderer(shapeName: String, render: ShapeRenderer): void {
		this.renderers.set(shapeName, render);
	}

	renderImageSource(canvas: CanvasImageSource): void {
		this.canvasContext.drawImage(canvas, 0, 0);
	}

	renderSurface(surface: RenderSurface): void {
		this.canvasContext.drawImage(surface.canvas, 0, 0);
	}

	renderShapes(shapes: Shape[]): void {
		for (let shape of shapes) {
			this.renderShape(shape, null);
		}
	}

	renderShape(shape: Shape, dirtyRegion: Rectangle): void {
		const renderer = this.renderers.get(shape.constructor.name);

		if (renderer) {
			const s = this.canvas.width * this.transform.getScaleX();
			const tx = this.transform.getTranslateX();
			const ty = this.transform.getTranslateY();

			this.canvasContext.save();
			this.canvasContext.scale(s, s);
			this.canvasContext.translate(-tx, -ty);

			renderer.render(this.canvasContext, shape, dirtyRegion);

			this.canvasContext.restore();
		}
	}

	getSize(): Dimension {
		return this.size;
	}

	setSize(width: number, height: number): void {
		// HiDPI handling
		const dpr = window.devicePixelRatio || 1;

		this.size = new Dimension(width * dpr, height * dpr);

		this.resizeCanvas(width, height, dpr);
		this.fireSizeEvent(new SizeEvent(this.size));
	}

	setTransform(transform: Transform): void {
		this.transform.setTransform(transform);
	}

	addSizeListener(listener: Listener<SizeEvent>): Disposable {
		return this.sizeEvent.subscribe(listener);
	}

	removeSizeListener(listener: Listener<SizeEvent>): void {
		this.sizeEvent.unsubscribe(listener);
	}

	protected fireSizeEvent(event: SizeEvent): void {
		this.sizeEvent.publish(event);
	}

	private resizeCanvas(width: number, height: number, devicePixelRatio: number): void {
		const scaleMode = this.canvas.getAttribute("scale");

		if (scaleMode && scaleMode === "full") {
			this.canvas.width = width * devicePixelRatio;
			this.canvas.height = height * devicePixelRatio;
		}

		this.canvas.style.width = width + "px";
		this.canvas.style.height = height + "px";
	}
}

export { RenderSurface };