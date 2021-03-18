import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { Point } from "../geometry/point";
import { TextMoveTool } from "../tool/text-move.tool";

class TextMoveAction extends Action {

	private readonly handle: number;

	private readonly point: Point;


	constructor(handle: number, point: Point) {
		super();

		this.handle = handle;
		this.point = point;
	}

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new TextMoveTool(this.handle, this.point));
	}

}

export { TextMoveAction };