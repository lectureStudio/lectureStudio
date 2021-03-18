import { Point } from "./point";
import { PenPoint } from "./pen-point";
import { Line } from "./line";
import { Rectangle } from "./rectangle";

class PenStroke {

	/** Temporary working space. */
	private readonly vector = new Point(0, 0);
	private readonly normal = new Point(0, 0);
	private readonly miter = new Point(0, 0);
	private readonly offsetA = new Point(0, 0);
	private readonly offsetB = new Point(0, 0);
	private readonly lastLineTop = new Line(0, 0, 0, 0);
	private readonly lastLineBottom = new Line(0, 0, 0, 0);

	/** Bottom line of the stroke. */
	private A = new Array<PenPoint>();

	/** Top line of the stroke. */
	private B = new Array<PenPoint>();

	/** Last two inserted points. */
	private points = new Array<PenPoint>();

	/** The stroke width. */
	private readonly strokeWidth: number;


	constructor(strokeWidth: number) {
		this.strokeWidth = strokeWidth;
	}

	getStrokeList(): Array<PenPoint> {
		if (this.points.length === 0) {
			return null;
		}

		const stroke = new Array<PenPoint>();

		// Special case with only one point.
		if (this.points.length == 1) {
			this.capOnePoint(stroke, this.points[0], this.strokeWidth);

			return stroke;
		}

		// Get bottom line.
		stroke.push(...this.A);

		// Cap last point.
		this.capEndpoint(stroke, this.points[1], this.points[0], false);

		// Get top line.
		stroke.push(...this.B);

		return stroke;
	}

	addPoint(point: PenPoint): void {
		this.points.push(point.clone());

		const pSize = this.points.length;

		if (pSize > 1) {
			if (pSize > 2) {
				// Keep track only of two last observed points.
				this.points.shift();
			}

			const p0 = this.points[0];

			if (this.A.length === 0 || this.B.length === 0) {
				// Cap first point.
				this.capEndpoint(this.A, p0, point, false);
				this.beginPath(p0, point);
			}
			else {
				this.advance(p0, point);
			}
		}
	}

	intersects(rect: Rectangle): boolean {
		// Handle simple cases.
		if (this.points.length === 0) {
			return false;
		}
		else if (this.points.length === 1) {
			return rect.containsPoint(this.points[0]);
		}

		if (this.intersectsRect(this.A, rect)) {
			return true;
		}
		if (this.intersectsRect(this.B, rect)) {
			return true;
		}

		return false;
	}

	moveByDelta(delta: Point): void {
		for (let point of this.A) {
			point.subtract(delta);
		}
		for (let point of this.B) {
			point.subtract(delta);
		}
		for (let point of this.points) {
			point.subtract(delta);
		}
	}

	clone(): PenStroke {
		const stroke = new PenStroke(this.strokeWidth);

		for (let point of this.A) {
			stroke.A.push(point.clone());
		}
		for (let point of this.B) {
			stroke.B.push(point.clone());
		}
		for (let point of this.points) {
			stroke.points.push(point.clone());
		}

		return stroke;
	}

	private advance(p0: PenPoint, p1: PenPoint): void {
		this.advancePath(this.A, p0, p1, false);
		this.advancePath(this.B, p0, p1, true);
	}

	private advancePath(target: Array<PenPoint>, p0: PenPoint, p1: PenPoint, reverse: boolean): void {
		this.vector.set(p1.x, p1.y).subtract(p0).normalize();
		this.normal.set(-this.vector.y, this.vector.x).normalize();

		this.miter.set(this.normal.x, this.normal.y).multiply(this.strokeWidth * this.toPressure(p0) / 2);
		this.offsetA.set(p0.x, p0.y);

		this.offset(this.offsetA, this.miter, reverse);

		this.miter.set(this.normal.x, this.normal.y).multiply(this.strokeWidth * this.toPressure(p1) / 2);
		this.offsetB.set(p1.x, p1.y);

		this.offset(this.offsetB, this.miter, reverse);

		const line = new Line(this.offsetA.x, this.offsetA.y, this.offsetB.x, this.offsetB.y);
		const lastLine = reverse ? this.lastLineTop : this.lastLineBottom;

		const inter = lastLine.getIntersectionPoint(line.x1, line.y1, line.x2, line.y2);

		if (inter != null) {
			this.intersect(target, inter, reverse);
		}
		else {
			const a = lastLine.getEndPoint();
			const b = line.getStartPoint();

			let s = this.toDegrees(a, p0);
			let e = this.toDegrees(b, p0);

			const d = Math.abs(s - e);

			if (d > 180) {
				s %= 360;
				e %= 360;
			}

			this.cap(target, p0, s, e, this.strokeWidth * this.toPressure(p0) / 2, 2, reverse);
		}

		lastLine.set(line.x1, line.y1, line.x2, line.y2);
	}

	private beginPath(p0: PenPoint, p1: PenPoint): void {
		this.vector.set(p1.x, p1.y).subtract(p0).normalize();
		this.normal.set(-this.vector.y, this.vector.x).normalize();

		// Bottom
		this.miter.set(this.normal.x, this.normal.y).multiply(this.strokeWidth * this.toPressure(p0) / 2);
		this.offsetA.set(p0.x, p0.y).add(this.miter);

		this.miter.set(this.normal.x, this.normal.y).multiply(this.strokeWidth * this.toPressure(p1) / 2);
		this.offsetB.set(p1.x, p1.y).add(this.miter);

		this.addPathPoint(this.A, this.offsetB.x, this.offsetB.y, false);

		this.lastLineBottom.set(this.offsetA.x, this.offsetA.y, this.offsetB.x, this.offsetB.y);

		// Top
		this.miter.set(this.normal.x, this.normal.y).multiply(this.strokeWidth * this.toPressure(p0) / 2);
		this.offsetA.set(p0.x, p0.y).subtract(this.miter);

		this.miter.set(this.normal.x, this.normal.y).multiply(this.strokeWidth * this.toPressure(p1) / 2);
		this.offsetB.set(p1.x, p1.y).subtract(this.miter);

		this.addPathPoint(this.B, this.offsetA.x, this.offsetA.y, true);
		this.addPathPoint(this.B, this.offsetB.x, this.offsetB.y, true);

		this.lastLineTop.set(this.offsetA.x, this.offsetA.y, this.offsetB.x, this.offsetB.y);
	}

	private cap(target: Array<PenPoint>, center: PenPoint, start: number, end: number, radius: number, step: number, reverse: boolean): void {
		if (start > end) {
			for (let angle = start; angle >= end; angle -= step) {
				const rad = Math.PI * angle / 180;

				const x = center.x + radius * Math.cos(rad);
				const y = center.y + radius * Math.sin(rad);

				this.addPathPoint(target, x, y, reverse);
			}
		}
		else {
			for (let angle = start; angle <= end; angle += step) {
				const rad = Math.PI * angle / 180;

				const x = center.x + radius * Math.cos(rad);
				const y = center.y + radius * Math.sin(rad);

				this.addPathPoint(target, x, y, reverse);
			}
		}
	}

	private capOnePoint(target: Array<PenPoint>, center: PenPoint, strokeWidth: number): void {
		this.addPathPoint(target, center.x + strokeWidth * this.toPressure(center) / 2, center.y, false);

		this.cap(target, center, 0, 360, strokeWidth * this.toPressure(center) / 2, 2, false);
	}

	private capEndpoint(target: Array<PenPoint>, p0: PenPoint, p1: PenPoint, reverse: boolean) {
		this.vector.set(p1.x, p1.y).subtract(p0).normalize();
		this.normal.set(-this.vector.y, this.vector.x).normalize();
		this.miter.set(this.normal.x, this.normal.y).multiply(this.strokeWidth * this.toPressure(p0) / 2);

		this.offsetA.set(p0.x, p0.y).add(this.miter);
		this.offsetB.set(p0.x, p0.y).subtract(this.miter);

		// First/Last point offset.
		this.addPathPoint(target, this.offsetB.x, this.offsetB.y, reverse);

		// Cap point with 180 degrees.
		const s = this.toDegrees(this.offsetB, this.offsetA);
		const e = s - 180;

		this.cap(target, p0, s, e, this.strokeWidth * this.toPressure(p0) / 2, 2, reverse);
	}

	private addPathPoint(target: Array<PenPoint>, x: number, y: number, reverse: boolean): void {
		if (reverse) {
			target.unshift(new PenPoint(x, y, 1));
		}
		else {
			target.push(new PenPoint(x, y, 1));
		}
	}

	private intersect(target: Array<PenPoint>, inter: Point, reverse: boolean): void {
		if (reverse) {
			target[0].set(inter.x, inter.y);
		}
		else {
			target[target.length - 1].set(inter.x, inter.y);
		}
	}

	private offset(center: Point, miter: Point, reverse: boolean): void {
		if (reverse) {
			center.subtract(miter);
		}
		else {
			center.add(miter);
		}
	}

	private toPressure(point: PenPoint): number {
		return Math.min(1.0, Math.sqrt(point.p + 0.1));
	}

	private toDegrees(p1: Point, p2: Point): number {
		const degrees = Math.atan2(p1.y - p2.y, p1.x - p2.x);

		return (2 * Math.PI + degrees) * 180 / Math.PI;
	}

	private intersectsRect(path: Array<PenPoint>, rect: Rectangle): boolean {
		let index = 0;
		let p1 = path[index++];
		let p2 = null;

		while (index < path.length) {
			p2 = path[index++];

			if (rect.intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
				return true;
			}

			p1 = p2;
		}

		return false;
	}
}

export { PenStroke };