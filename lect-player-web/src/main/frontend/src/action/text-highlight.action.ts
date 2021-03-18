import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { Rectangle } from "../geometry/rectangle";
import { TextHighlightTool } from "../tool/text-highlight.tool";
import { Color } from "../paint/color";

class TextHighlightAction extends Action {

	private readonly color: Color;

	private readonly textBounds: Rectangle[];


	constructor(color: Color, textBounds: Rectangle[]) {
		super();

		this.color = color;
		this.textBounds = textBounds;
	}

	execute(executor: ActionExecutor): void {
		executor.setKeyEvent(this.keyEvent);
		executor.setTool(new TextHighlightTool(this.color, this.textBounds));
	}
}

export { TextHighlightAction };