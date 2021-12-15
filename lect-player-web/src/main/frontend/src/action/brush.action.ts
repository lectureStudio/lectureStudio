import { Action } from "./action";
import { Brush } from "../paint/brush";

abstract class BrushAction extends Action {

	shapeHandle: number;

	brush: Brush;


	constructor(shapeHandle: number, brush?: Brush) {
		super();

		this.shapeHandle = shapeHandle;
		this.brush = brush;
	}

}

export { BrushAction };