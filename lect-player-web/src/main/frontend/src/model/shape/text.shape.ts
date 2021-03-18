import { TypesettingShape } from "./typesetting.shape";
import { ShapeEvent } from "./shape-event";
import { Font } from "../../paint/font";
import { Point } from "../../geometry/point";

class TextShape extends TypesettingShape {

	private font: Font;


	setFont(font: Font): void {
		if (this.font && this.font.equals(font)) {
			return;
		}

		this.font = font;

		this.fireShapeEvent(new ShapeEvent(this, this.bounds));
	}

	getFont(): Font {
		return this.font;
	}

	clone(): TextShape {
		const shape = new TextShape(this.getHandle());
		shape.setLocation(new Point(this.bounds.x, this.bounds.y));
		shape.setFont(this.font);
		shape.setTextColor(this.getTextColor());
		shape.setTextAttributes(new Map(this.getTextAttributes()));

		return shape;
	}
}

export { TextShape };