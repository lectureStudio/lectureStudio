import { ToolContext } from "./tool-context";
import { Tool } from "./tool";
import { PenPoint } from "../geometry/pen-point";
import { RemoveShapeAction } from "../model/action/remove-shape.action";

class RubberToolExt implements Tool {

	private shapeHandle: number;

	private context: ToolContext;


	constructor(shapeHandle: number) {
		this.shapeHandle = shapeHandle;
	}

	begin(point: PenPoint, context: ToolContext): void {
		this.context = context;
	}

	execute(point: PenPoint): void {
		const shape = this.context.page.getShapeByHandle(this.shapeHandle);

		if (shape) {
			this.context.page.addAction(new RemoveShapeAction([shape]));
		}
	}

	end(point: PenPoint): void {
		// No-op
	}
}

export { RubberToolExt };