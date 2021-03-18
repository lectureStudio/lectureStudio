import { Tool } from "./tool";
import { Point } from "../geometry/point";
import { ToolContext } from "./tool-context";
import { Rectangle } from "../geometry/rectangle";

class PanTool implements Tool {

	private context: ToolContext;

	private lastPoint: Point;


	begin(point: Point, context: ToolContext): void {
		this.context = context;
		this.lastPoint = point;
	}

	execute(point: Point): void {
		const slideShape = this.context.page.getSlideShape();
		const pageRect = slideShape.bounds;

		const x = pageRect.x + (this.lastPoint.x - point.x);
		const y = pageRect.y + (this.lastPoint.y - point.y);

		slideShape.setPageRect(new Rectangle(x, y, pageRect.width, pageRect.height));

		this.lastPoint = point;
	}

	end(point: Point): void {
		// No-op
	}
}

export { PanTool };