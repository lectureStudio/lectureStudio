import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { LatexTool } from "../tool/latex.tool";

class LatexAction extends Action {

	private readonly handle: number;


	constructor(handle: number) {
		super();

		this.handle = handle;
	}

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new LatexTool(this.handle));
	}

}

export { LatexAction };