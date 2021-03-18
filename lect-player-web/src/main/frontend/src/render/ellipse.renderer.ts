import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { EllipseShape } from "../model/shape/ellipse.shape";

class EllipseRenderer implements ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: EllipseShape, dirtyRegion: Rectangle): void {
		const p0 = shape.points[0];
		const p1 = shape.points[1];

		if (!p0 || !p1) {
			return;
		}

		const keyEvent = shape.getKeyEvent();
		const brush = shape.brush;
		const color = brush.color.toRgba();
		const fill = keyEvent && keyEvent.altKey;

		const minX = Math.min(p0.x, p1.x);
		const minY = Math.min(p0.y, p1.y);
		const maxX = Math.max(p0.x, p1.x);
		const maxY = Math.max(p0.y, p1.y);

		const radiusX = Math.abs(maxX - minX) * 0.5;
		const radiusY = Math.abs(maxY - minY) * 0.5;
		const centerX = minX + radiusX;
		const centerY = minY + radiusY;

		context.beginPath();

		if (fill) {
			context.fillStyle = color;
			context.ellipse(centerX, centerY, radiusX, radiusY, 0, 0, Math.PI * 2);
			context.fill();
		}
		else {
			context.strokeStyle = color;
			context.lineWidth = brush.width;
			context.ellipse(centerX, centerY, radiusX, radiusY, 0, 0, Math.PI * 2);
			context.stroke();
		}

		if (shape.isSelected()) {
			let w = 0;

			if (fill) {
				// Draw the selection indicator inside the ellipse.
				w = brush.width / 2;
			}

			context.beginPath();
			context.strokeStyle = "rgb(255, 0, 100)";
			context.lineWidth = brush.width;
			context.setLineDash([5 / context.getTransformExt().getScaleX()]);
			context.ellipse(centerX, centerY, radiusX - w, radiusY - w, 0, 0, Math.PI * 2);
			context.stroke();
		}
	}

}

export { EllipseRenderer };