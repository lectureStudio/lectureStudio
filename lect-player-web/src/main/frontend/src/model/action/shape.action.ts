import { Action } from "./action";
import { Shape } from "../shape/shape";
import { Page } from "../page";

abstract class ShapeAction implements Action<Page> {

	protected shapes: Shape[];


	constructor(shapes: Shape[]) {
		this.shapes = shapes;
	}


	abstract execute(page: Page): void;

	abstract undo(page: Page): void;

	abstract redo(page: Page): void;

}

export { ShapeAction };