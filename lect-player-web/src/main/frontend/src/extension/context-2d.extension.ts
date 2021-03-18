import { Transform } from "../geometry/transform";

declare global {

	interface CanvasRenderingContext2D {

		getTransformExt(): Transform;

	}

}


const _getTransform = CanvasRenderingContext2D.prototype.getTransform;

CanvasRenderingContext2D.prototype.getTransformExt = function () {
	if (_getTransform) {
		// This is the DOMMatrix.
		const m = _getTransform.apply(this, Array.from(arguments));

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext.setValues(m.a, m.b, m.c, m.d, m.e, m.f);
	}

	return this._transform_ext;
}


if (!_getTransform) {
	const _resetTransform = CanvasRenderingContext2D.prototype.resetTransform;
	const _rotate = CanvasRenderingContext2D.prototype.rotate;
	const _scale = CanvasRenderingContext2D.prototype.scale;
	const _setTransform = CanvasRenderingContext2D.prototype.setTransform;
	const _transform = CanvasRenderingContext2D.prototype.transform;
	const _translate = CanvasRenderingContext2D.prototype.translate;
	const _save = CanvasRenderingContext2D.prototype.save;
	const _restore = CanvasRenderingContext2D.prototype.restore;


	CanvasRenderingContext2D.prototype.resetTransform = function () {
		if (_resetTransform) {
			_resetTransform.apply(this, Array.from(arguments));
		}

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext.reset();
	}

	CanvasRenderingContext2D.prototype.rotate = function (angle: number) {
		if (_rotate) {
			_rotate.apply(this, Array.from(arguments));
		}

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext.rotate(angle);
	}

	CanvasRenderingContext2D.prototype.scale = function (x: number, y: number) {
		if (_scale) {
			_scale.apply(this, Array.from(arguments));
		}

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext.scale(x, y);
	}

	CanvasRenderingContext2D.prototype.setTransform = function (a?: number | DOMMatrix2DInit, b?: number, c?: number, d?: number, e?: number, f?: number) {
		if (_setTransform) {
			_setTransform.apply(this, Array.from(arguments));
		}

		if (b) {
			a = <number>a;
		}
		else {
			const domMatrix = <DOMMatrix2DInit>a;

			a = domMatrix.a;
			b = domMatrix.b;
			c = domMatrix.c;
			d = domMatrix.d;
			e = domMatrix.e;
			f = domMatrix.f;
		}

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext.setValues(a, b, c, d, e, f);
	}

	CanvasRenderingContext2D.prototype.transform = function (a: number, b: number, c: number, d: number, e: number, f: number) {
		if (_transform) {
			_transform.apply(this, Array.from(arguments));
		}

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext.multiply(new Transform([a, b, c, d, e, f]));
	}

	CanvasRenderingContext2D.prototype.translate = function (x: number, y: number) {
		if (_translate) {
			_translate.apply(this, Array.from(arguments));
		}

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext.translate(x, y);
	}

	CanvasRenderingContext2D.prototype.save = function () {
		if (_save) {
			_save.apply(this, Array.from(arguments));
		}

		if (!this._transform_ext_stack) {
			this._transform_ext_stack = [];
		}
		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}

		this._transform_ext_stack.push(this._transform_ext.clone());
	}

	CanvasRenderingContext2D.prototype.restore = function () {
		if (_restore) {
			_restore.apply(this, Array.from(arguments));
		}

		if (!this._transform_ext_stack) {
			this._transform_ext_stack = [];
		}

		this._transform_ext = this._transform_ext_stack.pop();

		if (!this._transform_ext) {
			this._transform_ext = new Transform();
		}
	}
}

export { }