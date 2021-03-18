import { Point } from "./point";

class Line {

	/** The x coordinate of the start point. */
	x1: number;

	/** The y coordinate of the start point. */
	y1: number;

	/** The x coordinate of the end point. */
	x2: number;

	/** The y coordinate of the end point. */
	y2: number;


	/**
	 * Constructs a Line from the start point (x1,y1) to the end point (x2,y2).
	 *
     * @param x1 the X coordinate of the start point.
     * @param y1 the Y coordinate of the start point.
     * @param x2 the X coordinate of the end point.
     * @param y2 the Y coordinate of the end point.
	 */
	constructor(x1: number, y1: number, x2: number, y2: number) {
		this.set(x1, y1, x2, y2);
	}

	/**
	 * Sets new start (x1,y1) and end (x2,y2) coordinates.
	 * 
	 * @param start the start point (x1,y1) of the line.
	 * @param end the end point (x2,y2) of the line.
	 */
	set(x1: number, y1: number, x2: number, y2: number): void {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	/**
	 * The start point (x1,y1) of the line represented by {@link Point}.
	 *
	 * @return the start point (x1,y1).
	 */
	getStartPoint(): Point {
		return new Point(this.x1, this.y1);
	}

	/**
	 * The end point (x2,y2) of the line represented by {@link Point}.
	 *
	 * @return the end point (x2,y2).
	 */
	getEndPoint(): Point {
		return new Point(this.x2, this.y2);
	}

	/**
	 * Returns the shortest distance from the specified point to this line.
	 * 
	 * @param x the X coordinate of the point.
	 * @param y the Y coordinate of the point.
	 * 
	 * @return the shortest distance from a point to this line.
	 */
	distance(x: number, y: number): number {
		const dx = this.x2 - this.x1;
		const dy = this.y2 - this.y1;
		const length = Math.sqrt(dx * dx + dy * dy);

		return Math.abs(((this.y1 - this.y2) * x + dx * y + (this.x1 * this.y2 - this.x2 * this.y1)) / length);
	}

	/**
	 * Tests if the specified line intersects this line.
	 * 
	 * @param line the line to check the intersection with.
	 * 
	 * @return true if the lines intersect each other, false otherwise.
	 */
	intersects(line: Line): boolean {
		return Line.intersects(this.x1, this.y1, this.x2, this.y2, line.x1, line.y1, line.x2, line.y2);
	}

	/**
	 * Computes the intersection point of this line segment and the provided line
	 * segment.
	 *
	 * @param x3 the X coordinate of the start point of the specified line segment.
	 * @param y3 the Y coordinate of the start point of the specified line segment.
	 * @param x4 the X coordinate of the end point of the specified line segment.
	 * @param y4 the Y coordinate of the end point of the specified line segment.
	 *
	 * @return the intersection point of the two line segments, null if the line
	 *         segments do not intersect each other.
	 */
	getIntersectionPoint(x3: number, y3: number, x4: number, y4: number): Point {
		const d = (y4 - y3) * (this.x2 - this.x1) - (x4 - x3) * (this.y2 - this.y1);

		// Are the lines parallel.
		if (Math.abs(d) < 0.01) {
			return null;
		}

		// Calculate the intermediate fractional point.
		const a = (x4 - x3) * (this.y1 - y3) - (y4 - y3) * (this.x1 - x3);
		const b = (this.x2 - this.x1) * (this.y1 - y3) - (this.y2 - this.y1) * (this.x1 - x3);
		const ua = a / d;
		const ub = b / d;

		// Test for intersection along the the segments.
		if (ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0) {
			const A1 = this.y2 - this.y1;
			const B1 = this.x1 - this.x2;
			const C1 = A1 * this.x1 + B1 * this.y1;
			const A2 = y4 - y3;
			const B2 = x3 - x4;
			const C2 = A2 * x3 + B2 * y3;

			return new Point((B2 * C1 - B1 * C2) / d, (A1 * C2 - A2 * C1) / d);
		}

		return null;
	}

	/**
	 * Tests if the line segment from (x1,y1) to (x2,y2) intersects the line segment
	 * from (x3,y3) to (x4,y4).
	 *
     * @param x1 the X coordinate of the start point of the first specified line segment.
     * @param y1 the Y coordinate of the start point of the first specified line segment.
     * @param x2 the X coordinate of the end point of the first specified line segment.
     * @param y2 the Y coordinate of the end point of the first specified line segment.
     * @param x3 the X coordinate of the start point of the second specified line segment.
     * @param y3 the Y coordinate of the start point of the second specified line segment.
     * @param x4 the X coordinate of the end point of the second specified line segment.
     * @param y4 the Y coordinate of the end point of the second specified line segment.
	 *
	 * @return true if the line segments intersect each other, false otherwise.
	 */
	static intersects(x1: number, y1: number, x2: number, y2: number, x3: number, y3: number, x4: number, y4: number): boolean {
		const d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

		// Are the lines parallel.
		if (d == 0) {
			return false;
		}

		// Calculate the intermediate fractional point.
		const a = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
		const b = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);
		const ua = a / d;
		const ub = b / d;

		// Test for intersection along the the segments.
		if (ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0) {
			return true;
		}

		return false;
	}
}

export { Line };