import { Tool } from "./tool";
import { ToolContext } from "./tool-context";
import { TextShape } from "../model/shape/text.shape";
import { PenPoint } from "../geometry/pen-point";

class TextTool implements Tool {

	private readonly handle: number;

	private shape: TextShape;

	private context: ToolContext;


	constructor(handle: number) {
		this.handle = handle;
	}

	begin(point: PenPoint, context: ToolContext): void {
		this.context = context;

		this.shape = new TextShape(this.handle);
	}

	execute(point: PenPoint): void {
		// No-op
	}

	end(point: PenPoint): void {
		this.shape.setLocation(point);

		this.context.page.addShape(this.shape);
	}

}

export { TextTool };