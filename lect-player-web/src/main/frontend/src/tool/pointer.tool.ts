import { ToolContext } from "./tool-context";
import { PaintTool } from "./paint.tool";
import { PenPoint } from "../geometry/pen-point";
import { Page } from "../model/page";
import { PointerShape } from "../model/shape/pointer.shape";

class PointerTool extends PaintTool {

	private shape: PointerShape;

	private page: Page;


	begin(point: PenPoint, context: ToolContext): void {
		this.page = context.page;

		this.shape = new PointerShape(this.brush);
		this.shape.setHandle(this.shapeHandle);
		this.shape.addPoint(point);

		this.page.addShape(this.shape);
	}

	execute(point: PenPoint): void {
		this.shape.addPoint(point);
	}

	end(point: PenPoint): void {
		this.page.removeShape(this.shape);
	}
}

export { PointerTool };