import { ActionExecutor } from "./action-executor";
import { ToolAction } from "./tool.action";

class ToolEndAction extends ToolAction {

	execute(executor: ActionExecutor): void {
		executor.endTool(this.point);
	}

}

export { ToolEndAction };