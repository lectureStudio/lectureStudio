import { AtomicTool } from "./atomic.tool";
import { Point } from "../geometry/point";
import { ToolContext } from "./tool-context";

class RedoTool extends AtomicTool {

	begin(point: Point, context: ToolContext): void {
		context.page.redo();
	}

}

export { RedoTool };