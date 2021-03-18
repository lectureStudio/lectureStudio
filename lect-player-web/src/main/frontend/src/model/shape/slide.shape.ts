import { Page } from "../page";
import { Shape } from "./shape";
import { Rectangle } from "../../geometry/rectangle";
import { ShapeEvent } from "./shape-event";

class SlideShape extends Shape {

	private readonly page: Page;


	constructor(page: Page) {
		super();

		this.page = page;

		this.updateBounds();
	}

	getPage(): Page {
		return this.page;
	}

	setPageRect(rect: Rectangle): void {
		if (this.bounds.equals(rect)) {
			return;
		}

		this.bounds.set(rect.x, rect.y, rect.width, rect.height);

		this.fireShapeEvent(new ShapeEvent(this, this.bounds));
	}

	protected updateBounds(): void {
		this.bounds.set(0, 0, 1, 1);
	}
}

export { SlideShape };