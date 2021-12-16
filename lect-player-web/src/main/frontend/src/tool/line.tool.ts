import { ToolContext } from "./tool-context";
import { PaintTool } from "./paint.tool";
import { PenPoint } from "../geometry/pen-point";
import { LineShape } from "../model/shape/line.shape";
import { AddShapeAction } from "../model/action/add-shape.action";

class LineTool extends PaintTool {

	private shape: LineShape;

	private context: ToolContext;


	begin(point: PenPoint, context: ToolContext): void {
		this.context = context;

		this.shape = new LineShape(this.brush);
		this.shape.setHandle(this.shapeHandle);
		this.shape.setP0(point);

		context.page.addAction(new AddShapeAction([this.shape]));
	}

	execute(point: PenPoint): void {
		this.shape.setKeyEvent(this.context.keyEvent);
		this.shape.setP1(point);
	}
}

export { LineTool };