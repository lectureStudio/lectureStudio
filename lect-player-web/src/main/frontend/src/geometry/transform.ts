import { Point } from "./point";

class Transform {

	/** Transformation matrix elements */
	private readonly m: number[];


	/**
	 * Creates a new Transform with a transformation matrix specified by an array
	 * consisting of [a, b, c, d, e, f] where the elements have the following
	 * representation:
	 * 
	 * @value a: the horizontal scaling factor
	 * @value b: the horizontal shearing factor
	 * @value c: the vertical shearing factor
	 * @value d: the vertical scaling factor
	 * @value e: the horizontal translation factor
	 * @value f: the vertical translation factor
	 * 
	 * If no transformation matrix is provided, the new transform is set to the
	 * identity transform.
	 * 
	 * @param m The transformation matrix elements.
	 */
	constructor(m?: number[]) {
		this.m = m ? m.slice(0, 6) : [1, 0, 0, 1, 0, 0];
	}

	/**
	 * @returns the horizontal scaling factor.
	 */
	getScaleX(): number {
		return this.m[0];
	}

	/**
	 * @returns the vertical scaling factor.
	 */
	getScaleY(): number {
		return this.m[3];
	}

	/**
	 * @returns the horizontal shearing factor.
	 */
	getShearX(): number {
		return this.m[1];
	}

	/**
	 * @returns the vertical shearing factor.
	 */
	getShearY(): number {
		return this.m[2];
	}

	/**
	 * @returns the horizontal translation factor.
	 */
	getTranslateX(): number {
		return this.m[4];
	}

	/**
	 * @returns the vertical translation factor.
	 */
	getTranslateY(): number {
		return this.m[5];
	}

	/**
	 * Creates a deep copy of this transform.
	 * 
	 * @return a copy of this transform.
	 */
	clone(): Transform {
		return new Transform(this.m);
	}

	/**
	 * Resets this transform to the identity transform.
	 */
	reset(): void {
		this.setValues(1, 0, 0, 1, 0, 0);
	}

	/**
	 * Sets the matrix elements of this transform to the matrix elements provided
	 * by the specified transform.
	 * 
	 * @param transform The transform to copy the matrix elements from.
	 */
	setTransform(transform: Transform): void {
		this.m[0] = transform.getScaleX();
		this.m[1] = transform.getShearX();
		this.m[2] = transform.getShearY();
		this.m[3] = transform.getScaleY();
		this.m[4] = transform.getTranslateX();
		this.m[5] = transform.getTranslateY();
	}

	/**
	 * Sets the matrix elements of this transform to the provided 2D matrix elements.
	 * 
	 * @param a the horizontal scaling factor.
	 * @param b the horizontal shearing factor.
	 * @param c the vertical shearing factor.
	 * @param d the vertical scaling factor.
	 * @param e the horizontal translation factor.
	 * @param f the vertical translation factor.
	 */
	setValues(a: number, b: number, c: number, d: number, e: number, f: number): void {
		this.m[0] = a;
		this.m[1] = b;
		this.m[2] = c;
		this.m[3] = d;
		this.m[4] = e;
		this.m[5] = f;
	}

	/**
	 * Inverts this transform in place.
	 */
	invert(): void {
		const d = 1 / (this.m[0] * this.m[3] - this.m[1] * this.m[2]);

		this.m[0] = this.m[3] * d;
		this.m[1] = -this.m[1] * d;
		this.m[2] = -this.m[2] * d;
		this.m[3] = this.m[0] * d;
		this.m[4] = d * (this.m[2] * this.m[5] - this.m[3] * this.m[4]);
		this.m[5] = d * (this.m[1] * this.m[4] - this.m[0] * this.m[5]);
	}

	/**
	 * Multiplies this transform with the provided transform in place.
	 * 
	 * @param transform The transform to multiply with.
	 */
	multiply(transform: Transform): void {
		this.m[0] = this.m[0] * transform.m[0] + this.m[2] * transform.m[1];
		this.m[1] = this.m[1] * transform.m[0] + this.m[3] * transform.m[1];
		this.m[2] = this.m[0] * transform.m[2] + this.m[2] * transform.m[3];
		this.m[3] = this.m[1] * transform.m[2] + this.m[3] * transform.m[3];
		this.m[4] = this.m[0] * transform.m[4] + this.m[2] * transform.m[5] + this.m[4];
		this.m[5] = this.m[1] * transform.m[4] + this.m[3] * transform.m[5] + this.m[5];
	}

	/**
	 * Concatenates this transform with a rotation transformation.
	 * 
	 * @param angle The angle of rotation measured in radians.
	 */
	rotate(angle: number): void {
		const c = Math.cos(angle);
		const s = Math.sin(angle);

		this.m[0] = this.m[0] * c + this.m[1] * s;
		this.m[1] = this.m[0] * -s + this.m[1] * c;
		this.m[2] = this.m[2] * c + this.m[3] * s;
		this.m[3] = this.m[2] * -s + this.m[3] * c;
	}

	/**
	 * Concatenates this transform with a scaling transformation.
	 * 
	 * @param sx the horizontal scaling factor.
	 * @param sy the vertical scaling factor.
	 */
	scale(sx: number, sy: number): void {
		this.m[0] *= sx;
		this.m[1] *= sx;
		this.m[2] *= sy;
		this.m[3] *= sy;
	}

	/**
	 * Concatenates this transform with a translate transformation.
	 * 
	 * @param x the distance to translate in the x direction.
	 * @param y the distance to translate in the y direction.
	 */
	translate(x: number, y: number): void {
		this.m[4] += this.m[0] * x + this.m[2] * y;
		this.m[5] += this.m[1] * x + this.m[3] * y;
	}

	/**
	 * Transforms a point represented by (x,y) coordinates.
	 * 
	 * @param px the x coordinate of the point to transform.
	 * @param py the y coordinate of the point to transform.
	 * 
	 * @return the transformed point.
	 */
	transformPoint(px: number, py: number): Point {
		const x = px;
		const y = py;

		px = x * this.m[0] + y * this.m[2] + this.m[4];
		py = x * this.m[1] + y * this.m[3] + this.m[5];

		return new Point(px, py);
	}

	/**
	 * Tests whether the provided transform describes the same transform.
	 * 
	 * @param other the transform to compare this transform to.
	 * 
	 * @return true if the transforms are equal, false otherwise.
	 */
	equals(other: Transform): boolean {
		if (!other) {
			return false;
		}

		return this.m.equals(other.m);
	}

	/**
	 * @return a string representation of this transform.
	 */
	toString(): string {
		return "[" + this.m.join(", ") + "]";
	}
}

export { Transform };