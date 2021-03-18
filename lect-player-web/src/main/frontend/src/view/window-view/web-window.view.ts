import { WindowView } from "../../api/view/window.view";
import { View } from "../../api/view/view";

class WebWindowView implements WindowView {

	constructor() {
		this.initialize();
	}

	initialize(): void {
		window.onload = () => {
			document.body.style.visibility = "visible";
		};
	}

	setView(view: View): void {
		if (!(view instanceof HTMLElement)) {
			throw new Error("View is expected to be of type HTMLElement");
		}

		document.body.removeChild(document.body.firstElementChild);
		document.body.appendChild(view);
	}
}

export { WebWindowView };