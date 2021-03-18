import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { PointerShape } from "../model/shape/pointer.shape";

class PointerRenderer implements ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: PointerShape, dirtyRegion: Rectangle): void {
		const point = shape.points[0];

		if (!point) {
			return;
		}

		const brush = shape.brush;
		const w = brush.width;
		const w2 = w / 2;

		context.beginPath();
		context.fillStyle = brush.color.toRgba();
		context.arc(point.x, point.y, w, 0, 2 * Math.PI);
		context.fill();

		// Draw inner ring.
		context.beginPath();
		context.strokeStyle = brush.color.toRgb();
		context.lineWidth = w / 4;
		context.arc(point.x, point.y, w2, 0, 2 * Math.PI);
		context.stroke();
	}

}

export { PointerRenderer };