import Mustache from "mustache";
import morphdom from "morphdom";

class WebViewElement extends HTMLElement {

	private readonly template: string;


	initialized(): boolean {
		return false;
	}

	protected render(): void {
		if (this.template) {
			const template = Mustache.render(this.template, this);

			morphdom(this.lastElementChild, template);
		}
	}

}

export { WebViewElement };