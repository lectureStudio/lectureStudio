import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { SelectGroupTool } from "../tool/select-group.tool";

class SelectGroupAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new SelectGroupTool());
	}

}

export { SelectGroupAction };