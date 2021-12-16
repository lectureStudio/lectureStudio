import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { RubberToolExt } from "../tool/rubber.tool-ext";

class RubberActionExt extends Action {

	shapeHandle: number;


	constructor(shapeHandle: number) {
		super();

		this.shapeHandle = shapeHandle;
	}

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new RubberToolExt(this.shapeHandle));
	}

}

export { RubberActionExt };