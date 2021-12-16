import { ToolContext } from "./tool-context";
import { PaintTool } from "./paint.tool";
import { PenPoint } from "../geometry/pen-point";
import { EllipseShape } from "../model/shape/ellipse.shape";
import { AddShapeAction } from "../model/action/add-shape.action";

class EllipseTool extends PaintTool {

	private shape: EllipseShape;

	private context: ToolContext;


	begin(point: PenPoint, context: ToolContext): void {
		this.context = context;

		this.shape = new EllipseShape(this.brush);
		this.shape.setHandle(this.shapeHandle);
		this.shape.setP0(point);

		context.page.addAction(new AddShapeAction([this.shape]));
	}

	execute(point: PenPoint): void {
		this.shape.setKeyEvent(this.context.keyEvent);
		this.shape.setP1(point);
	}
}

export { EllipseTool };