import { Action } from "./action";
import { Brush } from "../paint/brush";

abstract class BrushAction extends Action {

	brush: Brush;


	constructor(brush?: Brush) {
		super();

		this.brush = brush;
	}

}

export { BrushAction };