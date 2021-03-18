import { AtomicTool } from "./atomic.tool";
import { Point } from "../geometry/point";
import { ToolContext } from "./tool-context";

class UndoTool extends AtomicTool {

	begin(point: Point, context: ToolContext): void {
		context.page.undo();
	}

}

export { UndoTool };