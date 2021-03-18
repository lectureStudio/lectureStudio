import { ActionExecutor } from "./action-executor";
import { BrushAction } from "./brush.action";
import { ArrowTool } from "../tool/arrow.tool";

class ArrowAction extends BrushAction {

	execute(executor: ActionExecutor): void {
		const tool = new ArrowTool();
		tool.brush = this.brush;

		executor.setKeyEvent(this.keyEvent);
		executor.setTool(tool);
	}

}

export { ArrowAction };