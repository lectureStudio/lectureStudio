const RGB_COLOR_REGEX = /\((\d+),\s*(\d+),\s*(\d+)(,\s*(\d*.\d*))?\)/;

class Color {

	readonly r: number;
	readonly g: number;
	readonly b: number;
	readonly a: number;


	constructor(r: number, g: number, b: number, a?: number) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a || 1;
	}

	toHex(): string {
		return '#' + this.r.toString(16) + this.g.toString(16) + this.b.toString(16);
	}

	toRgb(): string {
		return `rgb(${this.r}, ${this.g}, ${this.b})`;
	}

	toRgba(): string {
		return `rgba(${this.r}, ${this.g}, ${this.b}, ${this.a})`;
	}

	toAlpha(alpha: number): string {
		return `rgba(${this.r}, ${this.g}, ${this.b}, ${alpha})`;
	}

	equals(other: Color): boolean {
		return this.a === other.a && this.r === other.r && this.g === other.g && this.b === other.b;
	}

	static fromHex(hex: string): Color {
		let s = hex.trim();

		if (s.indexOf('#') !== 0) {
			throw new Error("Not a hex color representation");
		}

		s = s.substr(s.indexOf('#') + 1);

		const r = parseInt(s.substr(0, 2), 16);
		const g = parseInt(s.substr(2, 2), 16);
		const b = parseInt(s.substr(4, 2), 16);

		return new Color(r, g, b);
	}

	static fromRGBString(hex: string): Color {
		let s = hex.trim();

		if (s.indexOf('rgb') !== 0) {
			throw new Error("Not a rgb color representation");
		}

		const res = RGB_COLOR_REGEX.exec(s);

		const r = parseInt(res[1], 10);
		const g = parseInt(res[2], 10);
		const b = parseInt(res[3], 10);
		const a = res[5] ? parseFloat(res[5]) : 1;

		return new Color(r, g, b, a);
	}

	static fromRGBNumber(rgba: number): Color {
		const a = ((rgba >> 24) & 0xFF) / 0xFF;
		const r = (rgba >> 16) & 0xFF;
		const g = (rgba >> 8) & 0xFF;
		const b = rgba & 0xFF;

		return new Color(r, g, b, a);
	}
}

export { Color };