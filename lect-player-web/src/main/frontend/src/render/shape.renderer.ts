import { Shape } from "../model/shape/shape";
import { Rectangle } from "../geometry/rectangle";

interface ShapeRenderer {

	render(context: CanvasRenderingContext2D, shape: Shape, dirtyRegion: Rectangle): void;

}

export { ShapeRenderer };