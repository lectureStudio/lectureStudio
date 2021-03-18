import { AtomicTool } from "./atomic.tool";
import { Point } from "../geometry/point";
import { ToolContext } from "./tool-context";
import { TypesettingShape } from "../model/shape/typesetting.shape";

class TextMoveTool extends AtomicTool {

	private readonly handle: number;

	private readonly point: Point;


	constructor(handle: number, point: Point) {
		super();

		this.handle = handle;
		this.point = point;
	}

	begin(point: Point, context: ToolContext): void {
		const shapes = context.page.getShapes();

		for (let shape of shapes) {
			if (shape instanceof TypesettingShape && shape.getHandle() === this.handle) {
				context.beginBulkRender();
				shape.setLocation(this.point);
				context.endBulkRender();
				break;
			}
		}
	}
}

export { TextMoveTool };