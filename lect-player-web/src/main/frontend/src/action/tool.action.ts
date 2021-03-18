import { Action } from "./action";
import { PenPoint } from "../geometry/pen-point";

abstract class ToolAction extends Action {

	point: PenPoint;


	constructor(point?: PenPoint) {
		super();

		this.point = point;
	}

}

export { ToolAction };