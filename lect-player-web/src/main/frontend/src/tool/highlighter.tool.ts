import { ToolContext } from "./tool-context";
import { PaintTool } from "./paint.tool";
import { PenPoint } from "../geometry/pen-point";
import { StrokeShape } from "../model/shape/stroke.shape";
import { AddShapeAction } from "../model/action/add-shape.action";

class HighlighterTool extends PaintTool {

	private shape: StrokeShape;


	begin(point: PenPoint, context: ToolContext): void {
		this.shape = new StrokeShape(this.brush);
		this.shape.setHandle(this.shapeHandle);
		this.shape.addPoint(point);

		context.page.addAction(new AddShapeAction([this.shape]));
	}

	execute(point: PenPoint): void {
		this.shape.addPoint(point);
	}
}

export { HighlighterTool };