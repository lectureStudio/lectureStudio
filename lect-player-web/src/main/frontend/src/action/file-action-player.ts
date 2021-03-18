import { Action } from "./action";
import { ActionPlayer } from "./action-player";
import { PageAction } from "./page.action";
import { RecordedPage } from "../model/recorded-page";
import { SlideDocument } from "../model/document";
import { ExecutableState } from "../utils/executable-state";
import { SyncState } from "../utils/sync-state";
import { Rectangle } from "../geometry/rectangle";
import { SimpleActionExecutor } from "./simple-action-executor";
import { RenderController } from "../render/render-controller";
import { FileActionExecutor } from "./file-action-executor";

class FileActionPlayer extends ActionPlayer {

	private readonly simpleExecutor: SimpleActionExecutor;

	private readonly recordedPages: RecordedPage[];

	private readonly syncState: SyncState;

	private pageChangeActions: Map<number, number>;

	private actions: Action[];

	private pageNumber: number;

	private requestID: number;


	constructor(document: SlideDocument, recordedPages: RecordedPage[], syncState: SyncState, renderController: RenderController) {
		super(document, new FileActionExecutor(document, renderController));

		this.recordedPages = recordedPages;
		this.syncState = syncState;
		this.simpleExecutor = new SimpleActionExecutor(document);
	}

	seekByTime(time: number): number {
		const pageNumber = this.getTimeTablePage(time);

		this.seek(pageNumber, time);

		return pageNumber;
	}

	seekByPage(pageNumber: number): number {
		const timestamp = this.pageChangeActions.get(pageNumber);

		if (timestamp == null) {
			return -1;
		}

		this.seek(pageNumber, timestamp);

		return timestamp;
	}

	protected initInternal(): void {
		this.pageNumber = 0;
		this.actions = [];
		this.pageChangeActions = new Map();

		for (let recPage of this.recordedPages) {
			this.pageChangeActions.set(recPage.pageNumber, recPage.timestamp);
		}

		this.resetPage(this.pageNumber);
		this.getPlaybackActions(this.pageNumber);
		
		this.executor.setPageNumber(this.pageNumber);
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

		this.reset();
		this.seekByPage(0);
	}

	protected suspendInternal(): void {
		cancelAnimationFrame(this.requestID);
	}

	protected destroyInternal(): void {
		this.actions.length = 0;
		this.pageChangeActions.clear();
	}

	protected executeActions(): void {
		let execute = true;

		while (execute) {
			const state = this.state;

			if (state === ExecutableState.Starting || state === ExecutableState.Started) {
				const time = this.syncState.audioTime;
				let actionCount = this.actions.length;

				if (actionCount > 0) {
					// Get next action for execution.
					const action = this.actions[actionCount - 1];

					if (time >= action.timestamp) {
						//console.log("action latency: " + (time - action.timestamp));

						action.execute(this.executor);

						// Remove executed action.
						this.actions.pop();

						actionCount = this.actions.length;
					}
					else {
						execute = false;
					}
				}
				else if (this.pageNumber < this.recordedPages.length - 1) {
					// Get actions for the next page.
					this.getPlaybackActions(++this.pageNumber);
				}
				else {
					execute = false;
				}
			}
			else {
				execute = false;
			}
		}
	}

	private run() {
		this.executeActions();

		this.requestID = requestAnimationFrame(this.run.bind(this));
	}

	private getPlaybackActions(pageNumber: number): void {
		const recPage = this.recordedPages[pageNumber];

		this.loadStaticShapes(recPage);

		// Add page change action.
		const action = new PageAction(pageNumber);
		action.timestamp = recPage.timestamp;

		this.actions.length = 0;
		this.actions.push(action);
		this.actions.push(...recPage.playbackActions);

		if (this.actions.length > 1) {
			this.actions.reverse();
		}

		this.pageNumber = pageNumber;
	}

	private getTimeTablePage(seekTime: number): number {
		let page = 0;

		for (let [pageNumber, timestamp] of this.pageChangeActions) {
			if (seekTime == timestamp) {
				page = pageNumber;
				break;
			}
			else if (seekTime < timestamp) {
				break;
			}
			page = pageNumber;
		}

		return page;
	}

	private seek(pageNumber: number, time: number): void {
		this.executor.setSeek(true);

		this.resetPages(pageNumber, this.pageNumber);

		const recPage = this.recordedPages[pageNumber];

		if (recPage.pageNumber === pageNumber) {
			this.getPlaybackActions(pageNumber);

			let actionCount = this.actions.length;

			// Find actions for execution on the given page.
			while (actionCount > 0) {
				const action = this.actions[actionCount - 1];

				if (time >= action.timestamp) {
					try {
						action.execute(this.executor);
					}
					catch (e) {
						console.error(e);
					}

					this.actions.pop();

					actionCount = this.actions.length;
				}
				else {
					// Nothing more to execute.
					break;
				}
			}
		}

		this.executor.setSeek(false);
	}

	private reset(): void {
		this.executor.setSeek(true);
		this.resetPages(0, this.pageNumber);
		this.executor.setSeek(false);
	}

	private resetPages(newPage: number, previousPage: number): void {
		if (newPage === previousPage) {
			this.resetPage(newPage);
		}
		else if (newPage < previousPage) {
			let resetPage = newPage;

			while (resetPage <= previousPage) {
				this.resetPage(resetPage++);
			}
		}
	}

	private resetPage(pageNumber: number): void {
		const page = this.document.getPage(pageNumber);

		if (page.hasShapes()) {
			page.clear();
		}

		page.getSlideShape().setPageRect(new Rectangle(0, 0, 1, 1));
	}

	private loadStaticShapes(recPage: RecordedPage): void {
		const staticActions = recPage.staticActions;
		let actionCount = staticActions.length;

		if (actionCount < 1) {
			return;
		}

		// Select the page to which to add static shapes.
		this.simpleExecutor.setPageNumber(recPage.pageNumber);

		for (let action of staticActions) {
			try {
				action.execute(this.simpleExecutor);
			}
			catch (e) {
				console.error(e);
			}
		}
	}
}

export { FileActionPlayer };