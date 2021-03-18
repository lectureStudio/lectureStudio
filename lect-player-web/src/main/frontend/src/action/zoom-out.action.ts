import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { ZoomOutTool } from "../tool/zoom-out.tool";

class ZoomOutAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.selectAndExecuteTool(new ZoomOutTool());
	}

}

export { ZoomOutAction };