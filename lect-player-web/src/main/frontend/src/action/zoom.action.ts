import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { ZoomTool } from "../tool/zoom.tool";

class ZoomAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new ZoomTool());
	}

}

export { ZoomAction };