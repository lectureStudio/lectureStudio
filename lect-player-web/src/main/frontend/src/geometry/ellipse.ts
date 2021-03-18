class Ellipse {

	/** The X coordinate of the upper-left corner of the Ellipse. */
	readonly x: number;

	/** The Y coordinate of the upper-left corner of the Ellipse. */
	readonly y: number;

	/** The overall width of the Ellipse. */
	readonly width: number;

	/** The overall height of the Ellipse. */
	readonly height: number;

	/** The major-axis radius of the Ellipse. */
	readonly radiusX: number;

	/** The minor-axis radius of the Ellipse. */
	readonly radiusY: number;

	/** The X coordinate of the center of the Ellipse. */
	readonly centerX: number;

	/** The Y coordinate of the center of the Ellipse. */
	readonly centerY: number;


	/**
	 * Constructs an Ellipse from the specified coordinates.
	 *
	 * @param x the X coordinate of the upper-left corner of the Ellipse.
	 * @param y the Y coordinate of the upper-left corner of the Ellipse.
	 * @param width the width of the Ellipse.
	 * @param height the height of the Ellipse.
	 */
	constructor(x: number, y: number, width: number, height: number) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.radiusX = width * 0.5;
		this.radiusY = height * 0.5;
		this.centerX = x + this.radiusX;
		this.centerY = y + this.radiusY;
	}

	/**
	 * Tests if the specified coordinates are inside the boundary of the Ellipse.
	 * 
	 * @param x the specified X coordinate to be tested.
	 * @param y the specified Y coordinate to be tested.
	 * 
	 * @return true if the specified coordinates are inside the Ellipse boundary,
	 *         false otherwise.
	 */
	containsPoint(x: number, y: number): boolean {
		const w = this.width;
		if (w <= 0.0) {
			return false;
		}

		const h = this.height;
		if (h <= 0.0) {
			return false;
		}

		const normX = (x - this.x) / w - 0.5;
		const normY = (y - this.y) / h - 0.5;

		return (normX * normX + normY * normY) < 0.25;
	}

	/**
	 * Tests if the Ellipse entirely contains the specified rectangle.
	 *
	 * @param x the X coordinate of the upper-left corner of the specified rectangle.
	 * @param y the Y coordinate of the upper-left corner of the specified rectangle.
	 * @param w the width of the specified rectangle.
	 * @param h the height of the specified rectangle.
	 * 
	 * @return true if the Ellipse entirely contains the specified rectangle,
	 *         false otherwise.
	 */
	containsRect(x: number, y: number, width: number, height: number): boolean {
		return (this.containsPoint(x, y) &&
			this.containsPoint(x + width, y) &&
			this.containsPoint(x, y + height) &&
			this.containsPoint(x + width, y + height));
	}

	/**
	 * Tests if the Ellipse intersects the specified rectangle.
	 *
	 * @param x the X coordinate of the upper-left corner of the specified rectangle.
	 * @param y the Y coordinate of the upper-left corner of the specified rectangle.
	 * @param w the width of the specified rectangle.
	 * @param h the height of the specified rectangle.
	 * 
	 * @return true if the Ellipse and the rectangle intersect each other,
	 *         false otherwise.
	 */
	intersectsRect(x: number, y: number, width: number, height: number): boolean {
		if (width <= 0.0 || height <= 0.0) {
			return false;
		}
		if (this.width <= 0.0 || this.height <= 0.0) {
			return false;
		}

		// Test whether the rectangle encloses the ellipse center.
		if (this.centerX >= x && this.centerY >= y && this.centerX <= x + width && this.centerY <= y + height) {
			return true;
		}

		return this.intersectsLine(x, y, x + width, y) ||
			this.intersectsLine(x, y + height, x + width, y + height) ||
			this.intersectsLine(x, y, x, y + height) ||
			this.intersectsLine(x + width, y, x + width, y + height);
	}

	/**
	 * Tests if the Ellipse intersects the line segment (x1,y1) to (x2,y2).
	 *
     * @param x1 the X coordinate of the start point of the line segment.
     * @param y1 the Y coordinate of the start point of the line segment.
     * @param x2 the X coordinate of the end point of the line segment.
     * @param y2 the Y coordinate of the end point of the line segment.
	 *
	 * @return true if the Ellipse and the line segment intersect each other,
	 *         false otherwise.
	 */
	intersectsLine(x1: number, y1: number, x2: number, y2: number): boolean {
		if (this.width <= 0.0 || this.height <= 0.0) {
			return false;
		}

		x1 -= this.centerX;
		x2 -= this.centerX;
		y1 -= this.centerY;
		y2 -= this.centerY;

		const rxsq = this.radiusX * this.radiusX;
		const rysq = this.radiusY * this.radiusY;

		const A = Math.pow(x2 - x1, 2) / rxsq + Math.pow(y2 - y1, 2) / rysq;
		const B = 2 * x1 * (x2 - x1) / rxsq + 2 * y1 * (y2 - y1) / rysq;
		const C = x1 * x1 / rxsq + y1 * y1 / rysq - 1;
		const D = B * B - 4 * A * C;

		if (D === 0) {
			const t = -B / 2 / A;

			return t >= 0 && t <= 1;
		}
		else if (D > 0) {
			const sqrt = Math.sqrt(D);
			const t1 = (-B + sqrt) / 2 / A;
			const t2 = (-B - sqrt) / 2 / A;

			return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
		}

		return false;
	}
}

export { Ellipse };