import { StrokeShape } from "./stroke.shape";
import { PenPoint } from "../../geometry/pen-point";
import { ShapeEvent } from "./shape-event";

class FormShape extends StrokeShape {

	setP0(point: PenPoint): boolean {
		if (this.points.length > 0) {
			let prev = this.points[0];

			if (point.equals(prev)) {
				return false;
			}

			this.points[0] = point;
		}
		else {
			this.points.push(point);
		}

		return true;
	}

	setP1(point: PenPoint): boolean {
		if (this.points.length > 1) {
			let prev = this.points[1];

			if (point.equals(prev)) {
				return false;
			}

			this.points[1] = point;
		}
		else {
			this.points.push(point);
		}

		this.updateBounds();
		this.fireShapeEvent(new ShapeEvent(this, this.bounds));

		return true;
	}

	protected updateBounds(): void {
		if (this.points.length < 2) {
			return;
		}

		const p0 = this.points[0];
		const p1 = this.points[1];

		if (p1.x < p0.x) {
			this.bounds.x = p1.x;
			this.bounds.width = p0.x - p1.x;
		}
		else {
			this.bounds.x = p0.x;
			this.bounds.width = p1.x - p0.x;
		}
		if (p1.y < p0.y) {
			this.bounds.y = p1.y;
			this.bounds.height = p0.y - p1.y;
		}
		else {
			this.bounds.y = p0.y;
			this.bounds.height = p1.y - p0.y;
		}
	}

	/**
	 * Indicates whether to fill the interior of the shape.
	 */
	protected fill(): boolean {
		const keyEvent = this.getKeyEvent();

		return keyEvent != null && keyEvent.altKey;
	}

}

export { FormShape };