import { ActionExecutor } from "./action-executor";
import { BrushAction } from "./brush.action";
import { HighlighterTool } from "../tool/highlighter.tool";

class HighlighterAction extends BrushAction {

	execute(executor: ActionExecutor): void {
		const tool = new HighlighterTool();
		tool.brush = this.brush;

		executor.setTool(tool);
	}

}

export { HighlighterAction };