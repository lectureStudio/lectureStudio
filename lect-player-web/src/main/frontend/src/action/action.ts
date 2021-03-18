import { ActionExecutor } from "./action-executor";

abstract class Action {

	keyEvent: KeyboardEvent;

	timestamp: number;


	abstract execute(executor: ActionExecutor): void;

}

export { Action };