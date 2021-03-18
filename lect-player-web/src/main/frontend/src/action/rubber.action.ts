import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { RubberTool } from "../tool/rubber.tool";

class RubberAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new RubberTool());
	}

}

export { RubberAction };