import { Dimension } from "../geometry/dimension";

class SizeEvent {

	private _size: Dimension;


	constructor(size: Dimension) {
		this._size = size;
	}

	get size(): Dimension {
		return this._size;
	}
}

export { SizeEvent };