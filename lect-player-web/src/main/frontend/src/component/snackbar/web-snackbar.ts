import { ViewElement } from "../../view/view-element";
import { WebViewElement } from "../../view/web-view-element";

type Position = "top-left" | "top-center" | "top-right" | "bottom-left" | "bottom-center" | "bottom-right";

type OnClose = () => void;

@ViewElement({
	selector: "web-snackbar",
	templateUrl: "web-snackbar.html",
	styleUrls: ["web-snackbar.css"],
})
class WebSnackbar extends WebViewElement {

	private onClose: OnClose;

	private position: Position;

	private text: string;

	private timeout: number;

	private timeoutId: number;

	private top: string;

	private bottom: string;


	constructor() {
		super();

		this.text = "";
		this.position = "top-center";
		this.timeout = 3000;
	}

	connectedCallback() {
		const transitionListener = (event: TransitionEvent) => {
			if (event.propertyName === 'opacity' && this.style.opacity === '0') {
				removeEventListener("transitionend", transitionListener);

				this.parentElement.removeChild(this);
			}
		};

		addEventListener("transitionend", transitionListener);

		this.querySelector("span").innerHTML = this.text;
		this.querySelector("svg").addEventListener("click", this.close.bind(this));

		this.top = getComputedStyle(this).top;
		this.bottom = getComputedStyle(this).bottom;

		this.classList.add(this.position);

		this.style.opacity = "1";

		this.timeoutId = window.setTimeout(this.close.bind(this), this.timeout);
	}

	setPosition(position: Position): void {
		this.position = position;
	}

	setText(text: string): void {
		this.text = text;
	}

	setTimeout(timeout: number): void {
		this.timeout = timeout;
	}

	setOnClose(onClose: OnClose): void {
		this.onClose = onClose;
	}

	show(): void {
		document.body.appendChild(this);
	}

	close(): void {
		window.clearTimeout(this.timeoutId);

		this.style.opacity = "0";

		// Move the snackbar to its origins.
		this.style.top = this.top;
		this.style.bottom = this.bottom;

		if (this.onClose) {
			this.onClose();
		}
	}
}

export { WebSnackbar };