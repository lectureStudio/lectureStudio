import { Tool } from "./tool";
import { ToolContext } from "./tool-context";
import { PenPoint } from "../geometry/pen-point";
import { LatexShape } from "../model/shape/latex.shape";

class LatexTool implements Tool {

	private readonly handle: number;

	private shape: LatexShape;

	private context: ToolContext;


	constructor(handle: number) {
		this.handle = handle;
	}

	begin(point: PenPoint, context: ToolContext): void {
		this.context = context;

		this.shape = new LatexShape(this.handle);
	}

	execute(point: PenPoint): void {
		// No-op
	}

	end(point: PenPoint): void {
		this.shape.setLocation(point);

		this.context.page.addShape(this.shape);
	}

}

export { LatexTool };