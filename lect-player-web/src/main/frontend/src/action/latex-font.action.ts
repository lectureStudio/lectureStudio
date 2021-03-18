import { Action } from "./action";
import { ActionExecutor } from "./action-executor";
import { Color } from "../paint/color";
import { Font } from "../paint/font";
import { LatexFontTool } from "../tool/latex-font.tool";

class LatexFontAction extends Action {

	private readonly handle: number;

	private readonly font: Font;

	private readonly textColor: Color;

	private readonly textAttributes: Map<string, boolean>;


	constructor(handle: number, font: Font, textColor: Color, textAttributes: Map<string, boolean>) {
		super();

		this.handle = handle;
		this.font = font;
		this.textColor = textColor;
		this.textAttributes = textAttributes;
	}	

	execute(executor: ActionExecutor): void {
		executor.selectAndExecuteTool(new LatexFontTool(this.handle, this.font, this.textColor, this.textAttributes));
	}

}

export { LatexFontAction };