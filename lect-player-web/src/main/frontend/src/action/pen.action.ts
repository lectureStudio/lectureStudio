import { ActionExecutor } from "./action-executor";
import { BrushAction } from "./brush.action";
import { PenTool } from "../tool/pen.tool";

class PenAction extends BrushAction {

	execute(executor: ActionExecutor): void {
		const tool = new PenTool();
		tool.brush = this.brush;

		executor.setTool(tool);
	}

}

export { PenAction };