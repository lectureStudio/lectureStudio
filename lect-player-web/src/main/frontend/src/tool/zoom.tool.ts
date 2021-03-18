import { ToolContext } from "./tool-context";
import { Tool } from "./tool";
import { ZoomShape } from "../model/shape/zoom.shape";
import { PenPoint } from "../geometry/pen-point";

class ZoomTool implements Tool {

	private shape: ZoomShape;

	private context: ToolContext;

	private initialized: boolean;


	begin(point: PenPoint, context: ToolContext): void {
		this.context = context;

		this.shape = new ZoomShape();

		this.context.page.addShape(this.shape);
	}

	execute(point: PenPoint): void {
		if (!this.initialized) {
			this.shape.setP0(point);
			this.initialized = true;
		}

		this.shape.setP1(point);
	}

	end(point: PenPoint): void {
		this.initialized = false;

		this.context.page.removeShape(this.shape);

		this.context.page.getSlideShape().setPageRect(this.shape.bounds);
	}
}

export { ZoomTool };