import { ShapeRenderer } from "./shape.renderer";
import { TextShape } from "../model/shape/text.shape";
import { Rectangle } from "../geometry/rectangle";
import { Font } from "../paint/font";

class TextRenderer implements ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: TextShape, dirtyRegion: Rectangle): void {
		const text = shape.getText();

		if (!text || text.length === 0) {
			return;
		}

		const bounds = shape.bounds;
		const font = shape.getFont();

		const transform = context.getTransformExt();
		const scale = transform.getScaleX();

		/*
		 * Render with identity transform and scaled font, since normalized
		 * font size won't give us the desired result as the text will be
		 * misplaced and missized.
		 */
		const scaledHeight = font.size * scale;
		const x = transform.getTranslateX() + bounds.x * scale;
		const y = transform.getTranslateY() + bounds.y * scale;

		const scaledFont = new Font(font.family, scaledHeight, font.style, font.weight);

		context.setTransform(1, 0, 0, 1, 0, 0);
		context.font = scaledFont.toString();
		context.fillStyle = shape.getTextColor().toRgba();
		context.fillText(text, x, y + scaledHeight);
	}
}

export { TextRenderer };