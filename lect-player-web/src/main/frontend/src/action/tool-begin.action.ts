import { ActionExecutor } from "./action-executor";
import { ToolAction } from "./tool.action";

class ToolBeginAction extends ToolAction {

	execute(executor: ActionExecutor): void {
		executor.beginTool(this.point);
	}

}

export { ToolBeginAction };