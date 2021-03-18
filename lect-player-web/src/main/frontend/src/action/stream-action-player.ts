import { ActionPlayer } from "./action-player";
import { SlideDocument } from "../model/document";
import { ActionExecutor } from "./action-executor";
import { SyncState } from "../utils/sync-state";
import { Action } from "./action";

class StreamActionPlayer extends ActionPlayer {

	private readonly syncState: SyncState;

	private actions: Action[];

	private requestID: number;



	constructor(document: SlideDocument, executor: ActionExecutor, syncState: SyncState) {
		super(document, executor);

		this.syncState = syncState;
	}

	addAction(action: Action): void {
		this.actions.push(action);
	}

	seekByTime(time: number): number {
		throw new Error("Method not implemented.");
	}

	seekByPage(pageNumber: number): number {
		throw new Error("Method not implemented.");
	}

	protected executeActions(): void {
		let actionCount = this.actions.length;

		while (actionCount > 0) {
			// Get next action for execution.
			const action = this.actions[actionCount - 1];
			const time = this.syncState.audioTime;

			if (time >= action.timestamp) {
				//console.log("action latency: " + (time - action.timestamp));

				action.execute(this.executor);

				// Remove executed action.
				this.actions.pop();

				actionCount = this.actions.length;
			}
			else {
				break;
			}
		}
	}

	protected initInternal(): void {
		this.actions = [];

		this.executor.setPageNumber(0);
	}

	protected startInternal(): void {
		try {
			this.run();
		}
		catch (e) {
			console.error(e);

			throw new Error("Execute action failed.");
		}
	}

	protected stopInternal(): void {
		cancelAnimationFrame(this.requestID);
	}

	protected suspendInternal(): void {
		cancelAnimationFrame(this.requestID);
	}

	protected destroyInternal(): void {
		this.actions.length = 0;
	}

	private run() {
		this.executeActions();

		this.requestID = requestAnimationFrame(this.run.bind(this));
	}

}

export { StreamActionPlayer };