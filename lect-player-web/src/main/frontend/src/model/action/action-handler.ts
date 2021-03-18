import { Action } from "./action";

class ActionHandler<T> {

	private undoActions: Action<T>[] = [];

	private redoActions: Action<T>[] = [];

	private model: T;


	constructor(model: T) {
		this.model = model;
	}

	executeAction(action: Action<T>): void {
		this.undoActions.push(action);
		this.redoActions.length = 0;

		action.execute(this.model);
	}

	undo(): void {
		if (this.undoActions.length < 1) {
			return;
		}

		const action = this.undoActions.pop();
		this.redoActions.push(action);
		action.undo(this.model);
	}

	redo(): void {
		if (this.redoActions.length < 1) {
			return;
		}

		const action = this.redoActions.pop();
		this.undoActions.push(action);
		action.redo(this.model);
	}

	clear(): void {
		this.undoActions.length = 0;
		this.redoActions.length = 0;
	}
}

export { ActionHandler };