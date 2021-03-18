import { Action } from "./action";
import { Color } from "../paint/color";

abstract class PaintAction extends Action {

	color: Color;

}

export { PaintAction };