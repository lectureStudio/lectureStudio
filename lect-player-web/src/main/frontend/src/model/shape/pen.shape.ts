import { StrokeShape } from "./stroke.shape";
import { PenStroke } from "../../geometry/pen-stroke";
import { Brush } from "../../paint/brush";
import { PenPoint } from "../../geometry/pen-point";
import { ShapeEvent } from "./shape-event";
import { Point } from "../../geometry/point";
import { Rectangle } from "../../geometry/rectangle";

class PenShape extends StrokeShape {

	private stroke: PenStroke;


	constructor(brush: Brush) {
		super(brush);

		this.stroke = new PenStroke(brush.width);
	}

	addPoint(point: PenPoint): boolean {
		// Keep only one point at a time.
		if (this.points.length > 0) {
			const prev = this.points[0];

			if (point.equals(prev)) {
				return false;
			}

			this.points[0] = point;
		}
		else {
			this.points.push(point);
		}

		this.stroke.addPoint(point);

		this.updateBounds();
		this.fireShapeEvent(new ShapeEvent(this, this.bounds));

		return true;
	}

	contains(point: PenPoint): boolean {
		return this.stroke.intersects(new Rectangle(point.x, point.y, point.x, point.y));
	}

	intersects(rect: Rectangle): boolean {
		return this.stroke.intersects(rect);
	}

	getPenStroke(): PenStroke {
		return this.stroke;
	}

	moveByDelta(delta: Point): void {
		this.stroke.moveByDelta(delta);

		this.updateBoundsByDelta(delta);

		this.fireShapeEvent(new ShapeEvent(this, this.bounds));
	}

	clone(): PenShape {
		const shape = new PenShape(this.brush.clone());
		shape.stroke = this.stroke.clone();
		shape.bounds.set(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
		shape.setKeyEvent(this.getKeyEvent());
		shape.setSelected(this.isSelected());

		return shape;
	}
}

export { PenShape };