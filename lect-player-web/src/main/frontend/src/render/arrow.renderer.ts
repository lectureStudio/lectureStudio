import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { ArrowShape } from "../model/shape/arrow.shape";
import { PenPoint } from "../geometry/pen-point";

class ArrowRenderer implements ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: ArrowShape, dirtyRegion: Rectangle): void {
		const p0 = shape.points[0];
		const p1 = shape.points[1];

		if (!p0 || !p1) {
			return;
		}

		const keyEvent = shape.getKeyEvent();
		const brush = shape.brush;

		context.save();

		this.createArrowPath(context, keyEvent, p0, p1, brush.width);

		context.fillStyle = brush.color.toRgba();
		context.fill();
		context.restore();

		if (shape.isSelected()) {
			const scale = context.getTransformExt().getScaleX();

			this.createArrowPath(context, keyEvent, p0, p1, brush.width);

			context.strokeStyle = "rgb(255, 0, 100)";
			context.lineWidth = 2 / scale;
			context.setLineDash([5 / scale]);
			context.stroke();
		}
	}

	private createArrowPath(context: CanvasRenderingContext2D, keyEvent: KeyboardEvent, p1: PenPoint, p2: PenPoint, width: number): void {
		const bold = keyEvent != null && keyEvent.altKey;
		const twoSided = keyEvent != null && keyEvent.shiftKey;

		const x1 = p1.x;
		const y1 = p1.y;
		const x2 = p2.x;
		const y2 = p2.y;
		const w = bold ? width * 2 : width;
		const wd = w / 2;
		const dx = x2 - x1;
		const dy = y2 - y1;
		const angle = Math.atan2(dy, dx);
		const length = Math.sqrt(dx * dx + dy * dy);

		const arrowRatio = 0.5;
		const arrowLength = w * 5;
		const waisting = 0.35;
		const veeX = length - w * 0.5 / arrowRatio;

		const waistX = length - arrowLength * 0.5;
		const waistY = arrowRatio * arrowLength * 0.5 * waisting;
		const arrowWidth = arrowRatio * arrowLength;
		const x = twoSided ? w * 0.5 / arrowRatio + arrowLength * 0.75 : 0;

		context.translate(x1, y1);
		context.rotate(angle);

		context.beginPath();
		context.moveTo(x, -wd);
		context.lineTo(veeX - arrowLength * 0.75, -wd);
		context.lineTo(veeX - arrowLength, -arrowWidth);
		context.quadraticCurveTo(waistX, -waistY, length, 0);
		context.quadraticCurveTo(waistX, waistY, veeX - arrowLength, arrowWidth);
		context.lineTo(veeX - arrowLength * 0.75, wd);
		context.lineTo(x, wd);

		if (twoSided) {
			const waistX = x - arrowLength * 0.5;
			const waistY = arrowRatio * arrowLength * 0.5 * waisting;

			context.lineTo(x + arrowLength * 0.25, arrowWidth);
			context.quadraticCurveTo(waistX, waistY, 0, 0);
			context.quadraticCurveTo(waistX, -waistY, x + arrowLength * 0.25, -arrowWidth);
		}

		context.closePath();
	}
}

export { ArrowRenderer };