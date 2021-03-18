import { Point } from "../geometry/point";
import { ToolContext } from "./tool-context";

interface Tool {

	begin(point: Point, context: ToolContext): void;

	execute(point: Point): void;

	end(point: Point): void;

}

export { Tool };