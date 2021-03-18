import { ShapeRenderer } from "./shape.renderer";
import { StrokeShape } from "../model/shape/stroke.shape";
import { Brush } from "../paint/brush";
import { Rectangle } from "../geometry/rectangle";

class HighlighterRenderer implements ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: StrokeShape, dirtyRegion: Rectangle): void {
		const points = shape.points;
		const pointCount = points.length;

		if (pointCount < 1) {
			return;
		}

		const brush = shape.brush;
		const color = shape.isSelected() ? "rgba(255, 0, 100, 0.5)" : brush.color.toRgba();

		let index = 0;
		let p0 = points[index++];

		if (index === pointCount) {
			this.drawCircle(context, brush, p0.x, p0.y, p0.p);
			return;
		}

		context.beginPath();
		context.strokeStyle = color;
		context.lineWidth = brush.width;
		context.lineCap = "round";
		context.lineJoin = "round";
		context.globalAlpha = 1;
		context.globalCompositeOperation = "multiply";

		context.moveTo(p0.x, p0.y);

		while (index < pointCount) {
			p0 = points[index++];

			context.lineTo(p0.x, p0.y);
		}

		context.stroke();
	}

	private drawCircle(context: CanvasRenderingContext2D, brush: Brush, x: number, y: number, p: number): void {
		const w = p * brush.width;
		const d = w / 2;

		context.beginPath();
		context.fillStyle = brush.color.toRgba();
		context.arc(x, y, d, 0, 2 * Math.PI);
		context.fill();
	}
}

export { HighlighterRenderer };