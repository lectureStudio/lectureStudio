import { Page } from "../model/page";
import { RenderController } from "../render/render-controller";

class ToolContext {

	private readonly renderController: RenderController;

	page: Page;

	keyEvent: KeyboardEvent;


	constructor(renderController: RenderController) {
		this.renderController = renderController;
	}

	beginBulkRender(): void {
		this.renderController.beginBulkRender();
	}

	endBulkRender(): void {
		this.renderController.endBulkRender();
	}
}

export { ToolContext };