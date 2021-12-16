import { ToolContext } from "./tool-context";
import { PaintTool } from "./paint.tool";
import { PenPoint } from "../geometry/pen-point";
import { RectangleShape } from "../model/shape/rectangle.shape";
import { AddShapeAction } from "../model/action/add-shape.action";

class RectangleTool extends PaintTool {

	private shape: RectangleShape;

	private context: ToolContext;


	begin(point: PenPoint, context: ToolContext): void {
		this.context = context;

		this.shape = new RectangleShape(this.brush);
		this.shape.setHandle(this.shapeHandle);
		this.shape.setP0(point);

		context.page.addAction(new AddShapeAction([this.shape]));
	}

	execute(point: PenPoint): void {
		this.shape.setKeyEvent(this.context.keyEvent);
		this.shape.setP1(point);
	}
}

export { RectangleTool };