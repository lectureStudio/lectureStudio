import { Tool } from "./tool";
import { Point } from "../geometry/point";
import { ToolContext } from "./tool-context";

abstract class AtomicTool implements Tool {

	abstract begin(point: Point, context: ToolContext): void;


	execute(point: Point): void {
		// No action
	}

	end(point: Point): void {
		// No action
	}
}

export { AtomicTool };