import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { SelectShape } from "../model/shape/select.shape";

class SelectRenderer implements ShapeRenderer {

	private readonly FRAME_COLOR = "#FF0064";


	render(context: CanvasRenderingContext2D, shape: SelectShape, dirtyRegion: Rectangle): void {
		const p0 = shape.points[0];
		const p1 = shape.points[1];

		if (!p0 || !p1) {
			return;
		}

		const width = 2 / context.getTransformExt().getScaleX();
		const dash = 4 / context.getTransformExt().getScaleY();

		context.beginPath();
		context.strokeStyle = this.FRAME_COLOR;
		context.lineWidth = width;
		context.lineDashOffset = 0;
		context.setLineDash([dash]);
		context.lineCap = "butt";
		context.lineJoin = "bevel";
		context.rect(p0.x, p0.y, p1.x - p0.x, p1.y - p0.y);
		context.stroke();
	}

}

export { SelectRenderer };