/**
 * A Dimension specifies a size through its width and height.
 */
class Dimension {

	/** The width of the dimension. */
	width: number;

	/** The height of the dimension. */
	height: number;


	/**
	 * Constructs a new Dimension with the specified size.
	 *
	 * @param width the width of the dimension.
	 * @param height the height of the dimension.
	 */
	constructor(width: number, height: number) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Tests whether the provided dimension describes the same dimension.
	 * 
	 * @param other the dimension to compare this dimension to.
	 * 
	 * @return true if the dimensions are equal, false otherwise.
	 */
	equals(other: Dimension): boolean {
		if (!other) {
			return false;
		}

		return this.width === other.height && this.height === other.height;
	}

	/**
	 * @return a string representation of this dimension.
	 */
	toString(): string {
		return `[${this.height}, ${this.width}]`;
	}
}

export { Dimension };