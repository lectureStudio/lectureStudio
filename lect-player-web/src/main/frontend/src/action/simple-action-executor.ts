import { Tool } from "../tool/tool";
import { ToolContext } from "../tool/tool-context";
import { SlideDocument } from "../model/document";
import { PenPoint } from "../geometry/pen-point";
import { AtomicTool } from "../tool/atomic.tool";
import { ActionExecutor } from "./action-executor";
import { Observer } from "../utils/observable";

class SimpleActionExecutor implements ActionExecutor {

	private readonly document: SlideDocument;

	private readonly toolContext: ToolContext;

	private tool: Tool;

	private previousTool: Tool;

	private pageNumber: number;


	constructor(document: SlideDocument) {
		this.document = document;
		this.toolContext = new ToolContext(null);
	}

	setOnSelectPageIndex(observer: Observer<number>): void {
		
	}

	setSeek(seek: boolean): void {
		
	}

	setKeyEvent(keyEvent: KeyboardEvent): void {
		this.toolContext.keyEvent = keyEvent;
	}

	setPageNumber(pageNumber: number): void {
		if (this.pageNumber === pageNumber) {
			return;
		}

		const page = this.document.getPage(pageNumber);

		this.pageNumber = pageNumber;
		this.toolContext.page = page;
	}

	setTool(tool: Tool): void {
		if (!tool) {
			throw new Error("Tool must not be null");
		}
		if (!this.tool && this.tool === tool) {
			return;
		}

		this.setPreviousTool(this.tool);

		this.tool = tool;
	}

	selectAndExecuteTool(tool: AtomicTool): void {
		const point = PenPoint.createZero();

		this.setTool(tool);

		this.beginTool(point);
		this.executeTool(point);
		this.endTool(point);

		this.setPreviousTool(this.previousTool);
	}

	beginTool(point: PenPoint): void {
		this.tool.begin(point.clone(), this.toolContext);
	}

	executeTool(point: PenPoint): void {
		this.tool.execute(point.clone());
	}

	endTool(point: PenPoint): void {
		this.tool.end(point.clone());
	}

	private setPreviousTool(tool: Tool): void {
		if (!tool) {
			return;
		}

		if (tool instanceof AtomicTool) {
			// Do not remember atomic tools.
			return;
		}

		this.previousTool = tool;
	}
}

export { SimpleActionExecutor };