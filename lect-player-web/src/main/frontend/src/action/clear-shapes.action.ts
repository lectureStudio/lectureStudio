import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { ClearShapesTool } from "../tool/clear-shapes.tool";

class ClearShapesAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new ClearShapesTool());
	}

}

export { ClearShapesAction };