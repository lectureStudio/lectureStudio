import { ActionExecutor } from "./action-executor";
import { BrushAction } from "./brush.action";
import { EllipseTool } from "../tool/ellipse.tool";

class EllipseAction extends BrushAction {

	execute(executor: ActionExecutor): void {
		const tool = new EllipseTool();
		tool.brush = this.brush;

		executor.setKeyEvent(this.keyEvent);
		executor.setTool(tool);
	}

}

export { EllipseAction };