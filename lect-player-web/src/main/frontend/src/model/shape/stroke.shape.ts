import { Shape } from "./shape";
import { ShapeEvent } from "./shape-event";
import { PenPoint } from "../../geometry/pen-point";
import { Brush } from "../../paint/brush";
import { Line } from "../../geometry/line";
import { Rectangle } from "../../geometry/rectangle";
import { Point } from "../../geometry/point";

class StrokeShape extends Shape {

	private readonly _brush: Brush;

	private minPoint = new Point(0, 0);

	private maxPoint = new Point(0, 0);


	constructor(brush: Brush) {
		super();

		this._brush = brush;
	}

	get brush(): Brush {
		return this._brush;
	}

	addPoint(point: PenPoint): boolean {
		const added = super.addPoint(point);

		if (added) {
			this.minPoint.x = Math.min(this.minPoint.x, point.x);
			this.minPoint.y = Math.min(this.minPoint.y, point.y);
			this.maxPoint.x = Math.max(this.maxPoint.x, point.x);
			this.maxPoint.y = Math.max(this.maxPoint.y, point.y);

			this.updateBounds();
			this.fireShapeEvent(new ShapeEvent(this, this.bounds));
		}

		return added;
	}

	contains(point: PenPoint): boolean {
		const delta = this.brush.width / 2;

		// Handle simple cases.
		if (this.points.length === 0) {
			return false;
		}
		else if (this.points.length === 1) {
			return this.points[0].distance(point) <= delta;
		}

		// One of these lines must be crossed by a segment of our stroke.
		const l1 = new Line(point.x + delta, point.y + delta, point.x - delta, point.y - delta);
		const l2 = new Line(point.x - delta, point.y + delta, point.x + delta, point.y - delta);

		let index = 0;
		let p1 = this.points[index++];
		let p2 = null;
		let segment = null;

		while (index < this.points.length) {
			p2 = this.points[index++];
			segment = new Line(p1.x, p1.y, p2.x, p2.y);

			if (segment.intersects(l1)) {
				return true;
			}
			if (segment.intersects(l2)) {
				return true;
			}

			p1 = p2;
		}

		return false;
	}

	intersects(rect: Rectangle): boolean {
		// Handle simple cases.
		if (this.points.length === 0) {
			return false;
		}
		else if (this.points.length === 1) {
			return rect.containsPoint(this.points[0]);
		}

		let index = 0;
		let p1 = this.points[index++];
		let p2 = null;

		while (index < this.points.length) {
			p2 = this.points[index++];

			if (rect.intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
				return true;
			}

			p1 = p2;
		}

		return false;
	}

	protected updateBounds(): void {
		const x = this.minPoint.x;
		const y = this.minPoint.y;
		const width = this.maxPoint.x - this.minPoint.x;
		const height = this.maxPoint.y - this.minPoint.y;

		this.bounds.set(x, y, width, height);
	}

	clone(): StrokeShape {
		const shape = new StrokeShape(this.brush.clone());
		shape.bounds.set(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
		shape.setKeyEvent(this.getKeyEvent());
		shape.setSelected(this.isSelected());

		for (let point of this.points) {
			shape.points.push(point.clone());
		}

		return shape;
	}
}

export { StrokeShape };