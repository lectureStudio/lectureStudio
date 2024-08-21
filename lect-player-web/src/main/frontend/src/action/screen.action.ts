import { Action } from "./action";
import { ActionExecutor } from "./action-executor";

class ScreenAction extends Action {

	execute(executor: ActionExecutor): void {
		// Ignore. Rendering will take place in other components, e.g., VideoReader.
	}

}

export { ScreenAction };