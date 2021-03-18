import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { UndoTool } from "../tool/undo.tool";

class UndoAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new UndoTool());
	}

}

export { UndoAction };