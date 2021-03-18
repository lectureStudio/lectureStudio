import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { RedoTool } from "../tool/redo.tool";

class RedoAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new RedoTool());
	}

}

export { RedoAction };