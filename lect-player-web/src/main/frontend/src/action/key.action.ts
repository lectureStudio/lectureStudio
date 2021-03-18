import { Action } from "./action";
import { ActionExecutor } from "./action-executor";

class KeyAction extends Action {

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
	}

}

export { KeyAction };