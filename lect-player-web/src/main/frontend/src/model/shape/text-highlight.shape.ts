import { Shape } from "./shape";
import { Rectangle } from "../../geometry/rectangle";
import { Point } from "../../geometry/point";
import { Color } from "../../paint/color";
import { ShapeEvent } from "./shape-event";

class TextHighlightShape extends Shape {

	private readonly color: Color;


	constructor(color: Color) {
		super();

		this.color = color;
	}

	addTextBounds(rect: Rectangle): void {
		if (this.bounds.isEmpty()) {
			this.bounds.set(rect.x, rect.y, rect.width, rect.height);
		}
		else {
			this.bounds.union(rect);
		}

		this.fireShapeEvent(new ShapeEvent(this, this.bounds));
	}

	getColor(): Color {
		return this.color;
	}

	contains(point: Point): boolean {
		return this.bounds.containsPoint(point);
	}

	intersects(rect: Rectangle): boolean {
		return this.bounds.intersection(rect) != null;
	}

	clone(): TextHighlightShape {
		const shape = new TextHighlightShape(this.color);
		shape.setKeyEvent(this.getKeyEvent());

		return shape;
	}

	protected updateBounds(): void {
		// No-op
	}
}

export { TextHighlightShape };