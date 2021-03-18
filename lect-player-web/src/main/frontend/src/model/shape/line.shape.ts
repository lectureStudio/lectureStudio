import { FormShape } from "./form.shape";

class LineShape extends FormShape {

	clone(): LineShape {
		const shape = new LineShape(this.brush.clone());
		shape.bounds.set(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
		shape.setKeyEvent(this.getKeyEvent());
		shape.setSelected(this.isSelected());

		for (let point of this.points) {
			shape.points.push(point.clone());
		}

		return shape;
	}

}

export { LineShape };