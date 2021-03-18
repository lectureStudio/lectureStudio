import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { TextTool } from "../tool/text.tool";

class TextAction extends Action {

	private readonly handle: number;


	constructor(handle: number) {
		super();

		this.handle = handle;
	}

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new TextTool(this.handle));
	}

}

export { TextAction };