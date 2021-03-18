import { ActionExecutor } from "./action-executor";
import { ExecutableBase } from "../utils/executable-base";
import { SlideDocument } from "../model/document";

abstract class ActionPlayer extends ExecutableBase {

	protected readonly document: SlideDocument;

	protected readonly executor: ActionExecutor;


	constructor(document: SlideDocument, executor: ActionExecutor) {
		super();

		this.document = document;
		this.executor = executor;
	}

	getDocument(): SlideDocument {
		return this.document;
	}

	getExecutor(): ActionExecutor {
		return this.executor;
	}



	abstract seekByTime(time: number): number;

	abstract seekByPage(pageNumber: number): number;

	protected abstract executeActions(): void;

}

export { ActionPlayer };