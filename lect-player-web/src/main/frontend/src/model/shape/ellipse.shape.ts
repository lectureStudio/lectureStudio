import { FormShape } from "./form.shape";
import { Point } from "../../geometry/point";
import { Ellipse } from "../../geometry/ellipse";
import { Rectangle } from "../../geometry/rectangle";

class EllipseShape extends FormShape {

	contains(point: Point): boolean {
		if (this.points.length < 2) {
			return false;
		}

		const ellipse = new Ellipse(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);

		if (this.fill()) {
			return ellipse.containsPoint(point.x, point.y);
		}

		return ellipse.intersectsLine(point.x, point.y, point.x + this.brush.width, point.y + this.brush.width);
	}

	intersects(rect: Rectangle): boolean {
		if (this.points.length < 2) {
			return false;
		}

		const ellipse = new Ellipse(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);

		return ellipse.intersectsRect(rect.x, rect.y, rect.width, rect.height);
	}

	clone(): EllipseShape {
		const shape = new EllipseShape(this.brush.clone());
		shape.bounds.set(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
		shape.setKeyEvent(this.getKeyEvent());
		shape.setSelected(this.isSelected());

		for (let point of this.points) {
			shape.points.push(point.clone());
		}

		return shape;
	}
}

export { EllipseShape };