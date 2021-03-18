import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { TextRemoveTool } from "../tool/text-remove.tool";

class TextRemoveAction extends Action {

	private readonly handle: number;


	constructor(handle: number) {
		super();

		this.handle = handle;
	}

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new TextRemoveTool(this.handle));
	}

}

export { TextRemoveAction };