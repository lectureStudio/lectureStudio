import { ActionExecutor } from "./action-executor";
import { ToolAction } from "./tool.action";

class ToolExecuteAction extends ToolAction {

	execute(executor: ActionExecutor): void {
		executor.executeTool(this.point);
	}

}

export { ToolExecuteAction };