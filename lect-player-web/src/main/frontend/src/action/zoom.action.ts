import { ActionExecutor } from "./action-executor";
import { BrushAction } from "./brush.action";
import { ZoomTool } from "../tool/zoom.tool";

class ZoomAction extends BrushAction {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new ZoomTool());
	}

}

export { ZoomAction };