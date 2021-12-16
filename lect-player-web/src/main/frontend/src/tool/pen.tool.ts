import { ToolContext } from "./tool-context";
import { PaintTool } from "./paint.tool";
import { PenPoint } from "../geometry/pen-point";
import { AddShapeAction } from "../model/action/add-shape.action";
import { PenShape } from "../model/shape/pen.shape";

class PenTool extends PaintTool {

	private shape: PenShape;


	begin(point: PenPoint, context: ToolContext): void {
		this.shape = new PenShape(this.brush);
		this.shape.setHandle(this.shapeHandle);
		this.shape.addPoint(point);

		context.page.addAction(new AddShapeAction([this.shape]));
	}

	execute(point: PenPoint): void {
		this.shape.addPoint(point);
	}

	end(point: PenPoint): void {
		// No-op
	}
}

export { PenTool };