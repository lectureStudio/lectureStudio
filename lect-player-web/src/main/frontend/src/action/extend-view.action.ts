import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { Rectangle } from "../geometry/rectangle";
import { ExtendViewTool } from "../tool/extend-view.tool";

class ExtendViewAction extends Action {

	private readonly rect: Rectangle;


	constructor(rect: Rectangle) {
		super();

		this.rect = rect;
	}

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.selectAndExecuteTool(new ExtendViewTool(this.rect));
	}

}

export { ExtendViewAction };