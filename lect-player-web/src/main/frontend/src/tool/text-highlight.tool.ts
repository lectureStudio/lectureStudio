import { Tool } from "./tool";
import { Point } from "../geometry/point";
import { Rectangle } from "../geometry/rectangle";
import { ToolContext } from "./tool-context";
import { TextHighlightShape } from "../model/shape/text-highlight.shape";
import { Color } from "../paint/color";
import { AddShapeAction } from "../model/action/add-shape.action";

class TextHighlightTool implements Tool {

	private readonly color: Color;

	private readonly textBounds: Rectangle[];

	private shape: TextHighlightShape;


	constructor(color: Color, textBounds: Rectangle[]) {
		this.color = color;
		this.textBounds = textBounds;
	}

	begin(point: Point, context: ToolContext): void {
		for (let rect of this.textBounds) {
			if (rect.containsPoint(point)) {
				if (!this.shape) {
					this.shape = new TextHighlightShape(this.color);

					context.page.addAction(new AddShapeAction([this.shape]));
				}

				this.shape.addTextBounds(rect);
			}
		}
	}

	execute(point: Point): void {
		for (let rect of this.textBounds) {
			if (rect.containsPoint(point)) {
				this.shape.addTextBounds(rect);
			}
		}
	}

	end(point: Point): void {
		this.shape = null;
	}
}

export { TextHighlightTool };