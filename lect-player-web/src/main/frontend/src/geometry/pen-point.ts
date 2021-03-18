import { Point } from "./point";

class PenPoint extends Point {

	/** The pressure. */
	p: number;


	constructor(x: number, y: number, p: number) {
		super(x, y);

		this.p = p;
	}

	clone(): PenPoint {
		return new PenPoint(this.x, this.y, this.p);
	}

	equals(other: PenPoint): boolean {
		if (!other) {
			return false;
		}
		return this.x === other.x && this.y === other.y && this.p === other.p;
	}

	static createZero(): PenPoint {
		return new PenPoint(0, 0, 0);
	}
}

export { PenPoint };