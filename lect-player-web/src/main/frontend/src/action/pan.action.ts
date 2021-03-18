import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { PanTool } from "../tool/pan.tool";

class PanAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new PanTool());
	}

}

export { PanAction };