/**
 * A Point object describes a point in the two dimensional space through
 * its (x,y) properties.
 */
class Point {

	/** The x coordinate. */
	x: number;

	/** The y coordinate. */
	y: number;


	/**
	 * Creates a new instance of Point with specified coordinates.
	 *
	 * @param x The x coordinate of the point.
	 * @param y The y coordinate of the point.
	 */
	constructor(x: number, y: number) {
		this.x = x;
		this.y = y;
	}

	set(x: number, y: number): Point {
		this.x = x;
		this.y = y;

		return this;
	}

	/**
	 * Returns the distance from this Point to a specified Point.
	 *
	 * @param p The point to which the distance should be measured.
	 *
	 * @return The distance to the given point.
	 */
	distance(point: Point): number {
		const dx = point.x - this.x;
		const dy = point.y - this.y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	add(point: Point): Point {
		this.x += point.x;
		this.y += point.y;

		return this;
	}

	subtract(point: Point): Point {
		this.x -= point.x;
		this.y -= point.y;

		return this;
	}

	multiply(scalar: number): Point {
		this.x *= scalar;
		this.y *= scalar;

		return this;
	}

	normalize(): Point {
		const length = Math.sqrt(this.x * this.x + this.y * this.y);

		this.x /= length;
		this.y /= length;

		return this;
	}

	/**
	 * Tests whether the provided point describes the same point.
	 *
	 * @param other the point to compare this point to.
	 *
	 * @return true if the points are equal, false otherwise.
	 */
	equals(other: Point): boolean {
		if (!other) {
			return false;
		}
		return this.x === other.x && this.y === other.y;
	}
}

export { Point };