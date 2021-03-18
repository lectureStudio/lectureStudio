import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { CloneTool } from "../tool/clone.tool";

class CloneAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new CloneTool());
	}

}

export { CloneAction };