import { Tool } from "./tool";
import { Point } from "../geometry/point";
import { Rectangle } from "../geometry/rectangle";
import { ToolContext } from "./tool-context";
import { TextHighlightShape } from "../model/shape/text-highlight.shape";
import { Color } from "../paint/color";
import { AddShapeAction } from "../model/action/add-shape.action";

class TextHighlightToolExt implements Tool {

	private readonly shapeHandle: number;

	private readonly color: Color;

	private readonly textBounds: Rectangle[];


	constructor(shapeHandle: number, color: Color, textBounds: Rectangle[]) {
		this.shapeHandle = shapeHandle;
		this.color = color;
		this.textBounds = textBounds;
	}

	begin(point: Point, context: ToolContext): void {
		for (let rect of this.textBounds) {
			let shape = context.page.getShapeByHandle(this.shapeHandle) as TextHighlightShape;

			if (!shape) {
				shape = new TextHighlightShape(this.color);
				shape.setHandle(this.shapeHandle);

				context.page.addAction(new AddShapeAction([shape]));
			}

			shape.addTextBounds(rect);
		}
	}

	execute(point: Point): void {
		// No-op
	}

	end(point: Point): void {
		// No-op
	}
}

export { TextHighlightToolExt };