import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { SelectTool } from "../tool/select.tool";

class SelectAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new SelectTool());
	}

}

export { SelectAction };