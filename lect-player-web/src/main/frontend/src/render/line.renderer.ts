import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { LineShape } from "../model/shape/line.shape";

class LineRenderer implements ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: LineShape, dirtyRegion: Rectangle): void {
		const p0 = shape.points[0];
		const p1 = shape.points[1];

		if (!p0 || !p1) {
			return;
		}

		const keyEvent = shape.getKeyEvent();
		const brush = shape.brush;
		const bold = keyEvent != null && keyEvent.altKey;
		const width = bold ? brush.width * 2 : brush.width;

		context.beginPath();
		context.strokeStyle = brush.color.toRgba();
		context.lineWidth = width;
		context.lineCap = "round";
		context.moveTo(p0.x, p0.y);
		context.lineTo(p1.x, p1.y);
		context.stroke();

		if (shape.isSelected()) {
			const scale = context.getTransformExt().getScaleX();
			const dashWidth = bold ? 7 / scale : 5 / scale;
			const dashDist = bold ? 15 / scale : 12 / scale;

			context.beginPath();
			context.strokeStyle = "rgb(255, 0, 100)";
			context.lineWidth = width;
			context.setLineDash([dashWidth, dashDist]);
			context.moveTo(p0.x, p0.y);
			context.lineTo(p1.x, p1.y);
			context.stroke();
		}
	}
}

export { LineRenderer };