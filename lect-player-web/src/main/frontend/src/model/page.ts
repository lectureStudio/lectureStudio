import { PageEvent, PageChangeType } from "./page-event";
import { Shape } from "./shape/shape";
import { ShapeEvent } from "./shape/shape-event";
import { TypedEvent, Listener, Disposable } from "../utils/event-listener";
import { Action } from "./action/action";
import { ActionHandler } from "./action/action-handler";
import { SlideShape } from "./shape/slide.shape";
import { SlideDocument } from "./document";

class Page {

	private readonly shapeChangeListener: (event: ShapeEvent) => void;

	private readonly actionHandler = new ActionHandler<Page>(this);

	private readonly changeEvent = new TypedEvent<PageEvent>();

	private readonly shapes: Shape[] = [];

	private readonly slideShape: SlideShape;

	private readonly document: SlideDocument;

	private readonly pageNumber: number;


	constructor(document: SlideDocument, pageNumber: number) {
		this.document = document;
		this.pageNumber = pageNumber;
		this.slideShape = new SlideShape(this);
		this.slideShape.addChangeListener(this.onSlideTransform.bind(this));

		this.shapeChangeListener = this.onShapeModified.bind(this);
	}

	getNativePage(): any {
		return this.document.getNativePage(this.pageNumber);
	}

	addChangeListener(listener: Listener<PageEvent>): Disposable {
		return this.changeEvent.subscribe(listener);
	}

	removeChangeListener(listener: Listener<PageEvent>): void {
		this.changeEvent.unsubscribe(listener);
	}

	getShapeByHandle(handle: number): Shape {
		for (const shape of this.shapes) {
			if (shape.getHandle() === handle) {
				return shape;
			}
		}

		return null;
	}

	addShape(shape: Shape) {
		const prevCount = this.shapes.length;

		if (this.shapes.push(shape) > prevCount) {
			shape.addChangeListener(this.shapeChangeListener);

			this.firePageEvent(new PageEvent(this, PageChangeType.ShapeAdded, shape));
		}
	}

	removeShape(shape: Shape) {
		const shapeIndex = this.shapes.indexOf(shape);

		if (shapeIndex > -1) {
			const deleted = this.shapes.splice(shapeIndex, 1);

			if (deleted.length > 0) {
				shape.removeChangeListener(this.shapeChangeListener);

				this.firePageEvent(new PageEvent(this, PageChangeType.ShapeRemoved, shape));
			}
		}
	}

	getSlideShape(): SlideShape {
		return this.slideShape;
	}

	getShapes(): Shape[] {
		return this.shapes;
	}

	hasShapes(): boolean {
		return this.shapes.length > 0;
	}

	addAction(action: Action<Page>) {
		this.actionHandler.executeAction(action);
	}

	undo(): void {
		this.actionHandler.undo();
	}

	redo(): void {
		this.actionHandler.redo();
	}

	clear(): void {
		this.actionHandler.clear();
		this.shapes.length = 0;

		this.firePageEvent(new PageEvent(this, PageChangeType.Clear, null));
	}

	private onSlideTransform(event: ShapeEvent): void {
		this.firePageEvent(new PageEvent(this, PageChangeType.PageTransform, event.shape, event.dirtyRegion));
	}

	private onShapeModified(event: ShapeEvent): void {
		this.firePageEvent(new PageEvent(this, PageChangeType.ShapeModified, event.shape, event.dirtyRegion));
	}

	private firePageEvent(event: PageEvent): void {
		this.changeEvent.publish(event);
	}
}

export { Page };