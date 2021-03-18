import { FormShape } from "./form.shape";
import { Rectangle } from "../../geometry/rectangle";
import { Point } from "../../geometry/point";
import { Line } from "../../geometry/line";

class RectangleShape extends FormShape {

	contains(point: Point): boolean {
		// Handle simple cases.
		if (this.points.length < 2) {
			return false;
		}

		if (this.fill()) {
			return this.bounds.containsPoint(point);
		}

		const delta = this.brush.width;

		let x1 = this.bounds.x;
		let y1 = this.bounds.y;
		let x2 = this.bounds.x + this.bounds.width;
		let y2 = this.bounds.y;

		if (this.intersectsSegment(delta, x1, y1, x2, y2, point)) {
			return true;
		}

		x1 = this.bounds.x + this.bounds.width;
		y1 = this.bounds.y;
		x2 = this.bounds.x + this.bounds.width;
		y2 = this.bounds.y + this.bounds.height;

		if (this.intersectsSegment(delta, x1, y1, x2, y2, point)) {
			return true;
		}

		x1 = this.bounds.x + this.bounds.width;
		y1 = this.bounds.y + this.bounds.height;
		x2 = this.bounds.x;
		y2 = this.bounds.y + this.bounds.height;

		if (this.intersectsSegment(delta, x1, y1, x2, y2, point)) {
			return true;
		}

		x1 = this.bounds.x;
		y1 = this.bounds.y;
		x2 = this.bounds.x;
		y2 = this.bounds.y + this.bounds.height;

		if (this.intersectsSegment(delta, x1, y1, x2, y2, point)) {
			return true;
		}

		return false;
	}

	intersects(rect: Rectangle): boolean {
		return this.bounds.intersection(rect) != null;
	}

	clone(): RectangleShape {
		const shape = new RectangleShape(this.brush.clone());
		shape.bounds.set(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
		shape.setKeyEvent(this.getKeyEvent());
		shape.setSelected(this.isSelected());

		for (let point of this.points) {
			shape.points.push(point.clone());
		}

		return shape;
	}

	private intersectsSegment(delta: number, x1: number, y1: number, x2: number, y2: number, p: Point): boolean {
		let x3 = p.x + delta;
		let y3 = p.y + delta;
		let x4 = p.x - delta;
		let y4 = p.y - delta;

		if (Line.intersects(x1, y1, x2, y2, x3, y3, x4, y4)) {
			return true;
		}

		x3 = p.x - delta;
		y3 = p.y + delta;
		x4 = p.x + delta;
		y4 = p.y - delta;

		if (Line.intersects(x1, y1, x2, y2, x3, y3, x4, y4)) {
			return true;
		}

		return false;
	}
}

export { RectangleShape };