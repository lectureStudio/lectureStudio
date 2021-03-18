import { Tool } from "./tool";
import { ToolContext } from "./tool-context";
import { PenPoint } from "../geometry/pen-point";
import { Shape } from "../model/shape/shape";
import { SelectShape } from "../model/shape/select.shape";
import { Rectangle } from "../geometry/rectangle";

enum Mode { Select, Move }

class SelectGroupTool implements Tool {

	private mode: Mode;
	
	private context: ToolContext;
	
	private shape: SelectShape;
	
	private selectedShapes: Shape[];
	
	private sourcePoint: PenPoint;
	
	private initialized: boolean;


	begin(point: PenPoint, context: ToolContext): void {
		this.sourcePoint = point.clone();
		this.context = context;
		this.initialized = false;

		this.shape = new SelectShape();

		this.context.page.addShape(this.shape);

		this.getSelectedShapes();

		if (this.hasSelectedShapes()) {
			if (this.hitSelected(point)) {
				this.mode = Mode.Move;
			}
			else {
				this.context.beginBulkRender();
				this.removeSelection();
				this.context.endBulkRender();

				this.mode = Mode.Select;
			}
		}
		else {
			this.mode = Mode.Select;
		}
	}

	execute(point: PenPoint): void {
		if (this.mode == Mode.Select) {
			if (!this.initialized) {
				this.shape.setP0(point);
				this.initialized = true;
			}

			this.shape.addPoint(point.clone());
			this.shape.setP1(point);

			this.selectGroup(this.shape.bounds);
		}
		else if (this.mode == Mode.Move) {
			this.sourcePoint.subtract(point);

			this.moveShapes(this.sourcePoint);

			this.sourcePoint = point.clone();
		}
	}

	end(point: PenPoint): void {
		this.context.page.removeShape(this.shape);

		this.initialized = false;
	}

	selectGroup(rect: Rectangle): void {
		this.context.beginBulkRender();

		this.removeSelection();

		for (let shape of this.context.page.getShapes()) {
			if (shape === this.shape) {
				continue;
			}
			if (shape.intersects(rect)) {
				this.addSelection(shape);
			}
		}

		this.context.endBulkRender();
	}

	private addSelection(shape: Shape): void {
		shape.setSelected(true);

		this.selectedShapes.push(shape);
	}

	private getSelectedShapes(): void {
		this.selectedShapes = new Array<Shape>();

		for (let shape of this.context.page.getShapes()) {
			if (shape.isSelected()) {
				this.selectedShapes.push(shape);
			}
		}
	}

	private hasSelectedShapes(): boolean {
		return this.selectedShapes.length != 0;
	}

	private hitSelected(point: PenPoint): boolean {
		for (let shape of this.selectedShapes) {
			if (shape.contains(point)) {
				return true;
			}
		}

		return false;
	}

	private removeSelection(): void {
		for (let shape of this.selectedShapes) {
			shape.setSelected(false);
		}

		this.selectedShapes.length = 0;
	}

	private moveShapes(delta: PenPoint): void {
		if (!this.hasSelectedShapes()) {
			return;
		}

		this.context.beginBulkRender();

		for (let shape of this.selectedShapes) {
			shape.moveByDelta(delta);
		}

		this.context.endBulkRender();
	}
}

export { SelectGroupTool };