import { Shape } from "./shape";
import { Rectangle } from "../../geometry/rectangle";

class ShapeEvent {

	private _shape: Shape;

	private _dirtyRegion: Rectangle;


	constructor(shape: Shape, dirtyRegion: Rectangle) {
		this._shape = shape;
		this._dirtyRegion = dirtyRegion;
	}

	get shape(): Shape {
		return this._shape;
	}

	get dirtyRegion(): Rectangle {
		return this._dirtyRegion;
	}
}

export { ShapeEvent };