import { Tool } from "./tool";
import { ToolContext } from "./tool-context";
import { Brush } from "../paint/brush";
import { PenPoint } from "../geometry/pen-point";

abstract class PaintTool implements Tool {

	brush: Brush;


	begin(point: PenPoint, context: ToolContext): void {
		
	}

	execute(point: PenPoint): void {
		
	}

	end(point: PenPoint): void {
		
	}
}

export { PaintTool };