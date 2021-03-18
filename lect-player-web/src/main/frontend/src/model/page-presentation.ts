import { Rectangle } from "../geometry/rectangle";
import { Observer } from "../utils/observable";
import { Property } from "../utils/property";
import { Page } from "./page";

class PagePresentation {

	private readonly page: Page;

	private readonly pageRect: Property<Rectangle>;


	constructor(page: Page) {
		this.page = page;
		this.pageRect = new Property(new Rectangle(0, 0, 4, 3));

		this.resetPageRect();
	}

	getPageRect(): Rectangle {
		return this.pageRect.value;
	}

	setPageRect(rect: Rectangle): void {
		this.pageRect.value.set(rect.x, rect.y, rect.width, rect.height);
		this.pageRect.notifyObservers();
	}

	resetPageRect(): void {
		this.pageRect.value.set(0, 0, 4, 3);
		this.pageRect.notifyObservers();
	}

	setOnPageRect(observer: Observer<Rectangle>): void {
		this.pageRect.subscribe(observer);
	}
}

export { PagePresentation };