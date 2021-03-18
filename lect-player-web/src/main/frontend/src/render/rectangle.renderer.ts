import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { RectangleShape } from "../model/shape/rectangle.shape";

class RectangleRenderer implements ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: RectangleShape, dirtyRegion: Rectangle): void {
		const bounds = shape.bounds;

		if (bounds.isEmpty()) {
			return;
		}

		const keyEvent = shape.getKeyEvent();
		const brush = shape.brush;
		const color = brush.color.toRgba();
		const fill = keyEvent && keyEvent.altKey;

		let x = bounds.x;
		let y = bounds.y;
		let w = bounds.width;
		let h = bounds.height;

		context.beginPath();

		if (fill) {
			context.fillStyle = color;
			context.rect(x, y, w, h);
			context.fill();
		}
		else {
			context.strokeStyle = color;
			context.lineWidth = brush.width;
			context.rect(x, y, w, h);
			context.stroke();
		}

		if (shape.isSelected()) {
			if (fill) {
				// Draw the selection indicator inside the rectangle.
				x += brush.width / 2;
				y += brush.width / 2;
				w -= brush.width;
				h -= brush.width;
			}

			context.beginPath();
			context.strokeStyle = "rgb(255, 0, 100)";
			context.lineWidth = brush.width;
			context.setLineDash([5 / context.getTransformExt().getScaleX()]);
			context.rect(x, y, w, h);
			context.stroke();
		}
	}
}

export { RectangleRenderer };