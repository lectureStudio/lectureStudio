import { ShapeAction } from "./shape.action";
import { Shape } from "../shape/shape";
import { Page } from "../page";

class RemoveShapeAction extends ShapeAction {

	constructor(shapes: Shape[]) {
		super(shapes);
	}

	execute(page: Page): void {
		for (let shape of this.shapes) {
			page.removeShape(shape);
		}
	}

	undo(page: Page): void {
		for (let shape of this.shapes) {
			page.addShape(shape);
		}
	}

	redo(page: Page): void {
		this.execute(page);
	}
}

export { RemoveShapeAction };