interface Action<T> {

	execute(model: T): void;

	undo(model: T): void;

	redo(model: T): void;

}

export { Action };