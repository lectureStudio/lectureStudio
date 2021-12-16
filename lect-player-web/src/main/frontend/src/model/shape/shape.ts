import { ShapeEvent } from "./shape-event";
import { PenPoint } from "../../geometry/pen-point";
import { Rectangle } from "../../geometry/rectangle";
import { TypedEvent, Listener, Disposable } from "../../utils/event-listener";
import { Point } from "../../geometry/point";

abstract class Shape {

	private readonly _points: PenPoint[] = [];

	private readonly _bounds: Rectangle = Rectangle.empty();

	private readonly changeEvent = new TypedEvent<ShapeEvent>();

	private shapeHandle: number;

	private selected: boolean;

	private keyEvent: KeyboardEvent;


	protected abstract updateBounds(): void;


	getHandle(): number {
		return this.shapeHandle;
	}

	setHandle(handle: number): void {
		this.shapeHandle = handle;
	}

	addPoint(point: PenPoint): boolean {
		const count = this._points.length;
		let last = null;

		if (count > 0) {
			last = this._points[count - 1];
		}
		if (point.equals(last)) {
			return false;
		}

		this._points.push(point);

		return true;
	}

	get points(): PenPoint[] {
		return this._points;
	}

	contains(point: PenPoint): boolean {
		return this._bounds.containsPoint(point);
	}

	intersects(rect: Rectangle): boolean {
		return this._bounds.intersection(rect) != null;
	}

	get bounds(): Rectangle {
		return this._bounds;
	}

	getKeyEvent(): KeyboardEvent {
		return this.keyEvent;
	}

	setKeyEvent(event: KeyboardEvent): void {
		this.keyEvent = event;
	}

	isSelected(): boolean {
		return this.selected;
	}

	setSelected(selected: boolean): void {
		if (this.selected == selected) {
			return;
		}

		this.selected = selected;

		this.fireShapeEvent(new ShapeEvent(this, this.bounds));
	}

	moveByDelta(delta: Point): void {
		for (let point of this._points) {
			point.subtract(delta);
		}

		this.updateBoundsByDelta(delta);

		this.fireShapeEvent(new ShapeEvent(this, this.bounds));
	}

	clone(): Shape {
		return Object.create(this);
	}

	addChangeListener(listener: Listener<ShapeEvent>): Disposable {
		return this.changeEvent.subscribe(listener);
	}

	removeChangeListener(listener: Listener<ShapeEvent>): void {
		this.changeEvent.unsubscribe(listener);
	}

	protected updateBoundsByDelta(delta: Point): void {
		this.bounds.x -= delta.x;
		this.bounds.y -= delta.y;
	}

	protected fireShapeEvent(event: ShapeEvent): void {
		this.changeEvent.publish(event);
	}
}

export { Shape };