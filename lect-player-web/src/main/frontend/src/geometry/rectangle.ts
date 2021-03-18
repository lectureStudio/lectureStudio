import { Point } from "./point";

const INSIDE = 0;
const LEFT = 1;
const RIGHT = 2;
const BOTTOM = 4;
const TOP = 8;

/**
 * A Rectangle specifies an area that is enclosed by it's top-left point (x,y)
 * and its width and height.
 */
class Rectangle {

	/** The x coordinate. */
	x: number;

	/** The y coordinate. */
	y: number;

	/** The width. */
	width: number;

	/** The height. */
	height: number;


	/**
	 * Creates a new instance of Rectangle with specified location coordinates and size.
	 *
	 * @param x the x coordinate of the rectangle.
	 * @param y the y coordinate of the rectangle.
	 * @param width the width of the rectangle.
	 * @param height the height of the rectangle.
	 */
	constructor(x: number, y: number, width: number, height: number) {
		this.set(x, y, width, height);
	}

	/**
	 * Set new location coordinates and size of this rectangle.
	 *
	 * @param x the x coordinate of the rectangle.
	 * @param y the y coordinate of the rectangle.
	 * @param width the width of the rectangle.
	 * @param height the height of the rectangle.
	 */
	set(x: number, y: number, width: number, height: number): void {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Determines whether the rectangle encloses some area.
	 *
	 * @return true if the rectangle is empty, false otherwise.
	 */
	isEmpty(): boolean {
		return (this.width <= 0.0) || (this.height <= 0.0);
	}

	/**
	 * Tests if the specified point is inside the boundary of the rectangle.
	 *
	 * @param point the point that represents a x and y coordinate pair.
	 *
	 * @return true if the specified point is inside the boundary, false otherwise.
	 */
	containsPoint(point: Point): boolean {
		const px = point.x;
		const py = point.y;

		return (px >= this.x && py >= this.y && px < this.x + this.width && py < this.y + this.height);
	}

	/**
	 * Tests if the interior of this rectangle entirely encloses the specified
	 * rectangle.
	 *
	 * @param rect The rectangle to check, whether it is enclosed by this rectangle.
	 *
	 * @return true if the interior of this rectangle entirely contains the
	 *         specified area, false otherwise.
	 */
	containsRect(rect: Rectangle): boolean {
		if (this.isEmpty() || rect.isEmpty()) {
			return false;
		}

		const x = rect.x;
		const y = rect.y;
		const w = rect.width;
		const h = rect.height;
		const x0 = this.x;
		const y0 = this.y;

		return (x >= x0 && y >= y0 && (x + w) <= x0 + this.width && (y + h) <= y0 + this.height);
	}

	/**
	 * Tests if the line segment from (x1,y1) to (x2,y2) intersects this rectangle.
	 * 
	 * @param x1 the X coordinate of the start point of the line segment.
	 * @param y1 the Y coordinate of the start point of the line segment.
	 * @param x2 the X coordinate of the end point of the line segment.
	 * @param y2 the Y coordinate of the end point of the line segment.
	 * 
	 * @return true if the line segment intersects this rectangle, false otherwise.
	 */
	intersectsLine(x1: number, y1: number, x2: number, y2: number): boolean {
		const xmin = this.x;
		const xmax = this.x + this.width;
		const ymin = this.y;
		const ymax = this.y + this.height;

		let outcode0 = this.computeCode(x1, y1, xmin, xmax, ymin, ymax);
		let outcode1 = this.computeCode(x2, y2, xmin, xmax, ymin, ymax);

		while (true) {
			if (!(outcode0 | outcode1)) {
				return true;
			}
			else if (outcode0 & outcode1) {
				return false;
			}
			else {
				let outcodeOut = outcode0 ? outcode0 : outcode1;
				let x, y;

				if (outcodeOut & TOP) {
					x = x1 + (x2 - x1) * (ymax - y1) / (y2 - y1);
					y = ymax;
				}
				else if (outcodeOut & BOTTOM) {
					x = x1 + (x2 - x1) * (ymin - y1) / (y2 - y1);
					y = ymin;
				}
				else if (outcodeOut & RIGHT) {
					y = y1 + (y2 - y1) * (xmax - x1) / (x2 - x1);
					x = xmax;
				}
				else if (outcodeOut & LEFT) {
					y = y1 + (y2 - y1) * (xmin - x1) / (x2 - x1);
					x = xmin;
				}

				if (outcodeOut == outcode0) {
					x1 = x;
					y1 = y;
					outcode0 = this.computeCode(x1, y1, xmin, xmax, ymin, ymax);
				}
				else {
					x2 = x;
					y2 = y;
					outcode1 = this.computeCode(x2, y2, xmin, xmax, ymin, ymax);
				}
			}
		}
	}

	/**
	 * Intersects the provided rectangle with this one and puts the result into the
	 * returned rectangle object.
	 *
	 * @param rect The rectangle to be intersected with this one.
	 *
	 * @return the intersection rectangle, or null if the rectangles don't intersect
	 *         each other.
	 */
	intersection(rect: Rectangle): Rectangle {
		const iX = Math.max(this.x, rect.x);
		const iY = Math.max(this.y, rect.y);
		const iW = Math.min(this.x + this.width, rect.x + rect.width) - iX;
		const iH = Math.min(this.y + this.height, rect.y + rect.height) - iY;

		if (iW <= 0) {
			return null;
		}
		if (iH <= 0) {
			return null;
		}

		return new Rectangle(iX, iY, iW, iH);
	}

	/**
	 * Unions the provided rectangle with this one and puts the result into
	 * this rectangle object.
	 * 
	 * @param rect The rectangle to be combined with this one.
	 */
	union(rect: Rectangle): void {
		const x1 = Math.min(this.x, rect.x);
		const y1 = Math.min(this.y, rect.y);
		const x2 = Math.max(this.x + this.width, rect.x + rect.width);
		const y2 = Math.max(this.y + this.height, rect.y + rect.height);

		this.set(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * Tests whether the provided rectangle describes the same rectangle.
	 *
	 * @param other the rectangle to compare this rectangle to.
	 *
	 * @return true if the rectangles are equal, false otherwise.
	 */
	equals(other: Rectangle): boolean {
		if (!other) {
			return false;
		}

		return this.x === other.x && this.y === other.y && this.width === other.width && this.height === other.height;
	}

	/**
	 * @return a string representation of this rectangle.
	 */
	toString(): string {
		return `[${this.x}, ${this.y}, ${this.height}, ${this.width}]`;
	}

	/**
	 * @return a new empty rectangle which encloses no area.
	 */
	static empty(): Rectangle {
		return new Rectangle(0, 0, 0, 0);
	}

	private computeCode(x: number, y: number, xmin: number, xmax: number, ymin: number, ymax: number): number {
		let code = INSIDE;

		if (x < xmin) {
			code |= LEFT;
		}
		else if (x > xmax) {
			code |= RIGHT;
		}

		if (y < ymin) {
			code |= BOTTOM;
		}
		else if (y > ymax) {
			code |= TOP;
		}

		return code;
	}
}

export { Rectangle };