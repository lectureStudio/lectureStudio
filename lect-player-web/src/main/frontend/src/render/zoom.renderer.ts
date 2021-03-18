import { ShapeRenderer } from "./shape.renderer";
import { Rectangle } from "../geometry/rectangle";
import { ZoomShape } from "../model/shape/zoom.shape";

class ZoomRenderer implements ShapeRenderer {

	private readonly FILL_COLOR = "rgba(0, 163, 232, 0.4)";

	private readonly FRAME_COLOR = "rgb(255, 0, 100)";


	render(context: CanvasRenderingContext2D, shape: ZoomShape, dirtyRegion: Rectangle): void {
		const bounds = shape.bounds;

		context.fillStyle = this.FILL_COLOR;
		context.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		context.beginPath();
		context.strokeStyle = this.FRAME_COLOR;
		context.lineWidth = 2 / context.getTransformExt().getScaleX();
		context.rect(bounds.x, bounds.y, bounds.width, bounds.height);
		context.stroke();
	}

}

export { ZoomRenderer };