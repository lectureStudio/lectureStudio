class Font {

	readonly family: string;

	readonly size: number;

	readonly style: string;

	readonly weight: string;


	constructor(family: string, size: number, style?: string, weight?: string) {
		this.family = family;
		this.size = size;
		this.style = style;
		this.weight = weight;
	}

	equals(other: Font): boolean {
		if (!other) {
			return false;
		}
		if (this === other) {
			return true;
		}

		return this.family === other.family &&
			this.size === other.size &&
			this.style === other.style &&
			this.weight === other.weight;
	}

	toString(): string {
		let str = this.size + "px " + this.family;

		if (this.weight) {
			str = this.weight + " " + str;
		}
		if (this.style) {
			str = this.style + " " + str;
		}

		return str;
	}
}

export { Font };