import { Tool } from "../tool/tool";
import { AtomicTool } from "../tool/atomic.tool";
import { PenPoint } from "../geometry/pen-point";
import { Observer } from "../utils/observable";

interface ActionExecutor {

	setOnSelectPageIndex(observer: Observer<number>): void;

	setSeek(seek: boolean): void;

	setKeyEvent(keyEvent: KeyboardEvent): void;

	setPageNumber(pageNumber: number): void;

	setTool(tool: Tool): void;

	selectAndExecuteTool(tool: AtomicTool): void;

	beginTool(point: PenPoint): void;

	executeTool(point: PenPoint): void;

	endTool(point: PenPoint): void;

}

export { ActionExecutor };