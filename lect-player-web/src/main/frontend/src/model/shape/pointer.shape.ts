import { PenPoint } from "../../geometry/pen-point";
import { StrokeShape } from "./stroke.shape";
import { ShapeEvent } from "./shape-event";

class PointerShape extends StrokeShape {

	addPoint(point: PenPoint): boolean {
		// Keep only one point at a time.
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

		this.updateBounds();
		this.fireShapeEvent(new ShapeEvent(this, this.bounds));

		return true;
	}

	protected updateBounds(): void {
		const p0 = this.points[0];
		const d = this.brush.width;

		this.bounds.set(p0.x - d, p0.y - d, p0.x + d * 2, p0.y + d * 2);
	}
}

export { PointerShape };