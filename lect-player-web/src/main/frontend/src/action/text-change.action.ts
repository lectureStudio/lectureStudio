import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { TextChangeTool } from "../tool/text-change.tool";

class TextChangeAction extends Action {

	private readonly handle: number;

	private readonly text: string;


	constructor(handle: number, text: string) {
		super();

		this.handle = handle;
		this.text = text;
	}

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new TextChangeTool(this.handle, this.text));
	}

}

export { TextChangeAction };