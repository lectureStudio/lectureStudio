import { ViewElement } from "../../view/view-element";
import { WebViewElement } from "../../view/web-view-element";

type ValueFormatter = (value: string) => string;

@ViewElement({
	selector: "web-slider",
	templateUrl: "web-slider.html",
	styleUrls: ["web-slider.css"]
})
class WebSlider extends WebViewElement {

	private slider: HTMLInputElement;

	private progressElement: HTMLElement;

	private bubbleElement: HTMLElement;

	private thumbElement: HTMLElement;

	private valueFormatter: ValueFormatter;


	constructor() {
		super();
	}

	connectedCallback() {
		if (this.progress) {
			this.progressElement = document.createElement("div");
			this.progressElement.classList.add("progress");
			this.appendChild(this.progressElement);
		}
		if (this.bubble) {
			this.bubbleElement = document.createElement("div");
			this.bubbleElement.classList.add("bubble");
			this.appendChild(this.bubbleElement);
		}

		this.thumbElement = document.createElement("div");
		this.thumbElement.classList.add("thumb");
		this.appendChild(this.thumbElement);

		this.slider.addEventListener("input", this.onInput.bind(this), false);

		this.min = this.getAttribute("min");
		this.max = this.getAttribute("max");
		this.value = this.getAttribute("value");
		this.step = this.getAttribute("step");
	}

	attributeChangedCallback(attrName: string, oldVal: string, newVal: string) {
		if (this.slider) {
			switch (attrName) {
				case "min":
					this.slider.min = newVal;
					break;

				case "max":
					this.slider.max = newVal;
					break;

				case "value":
					this.slider.value = newVal;
					break;

				case "step":
					this.slider.step = newVal;
					break;
			}

			this.onInput();
		}
	}

	static get observedAttributes() {
		return ["min", "max", "value", "step", "bubble", "progress"];
	}

	get bubble(): boolean {
		return this.hasAttribute("bubble");
	}

	set bubble(value: boolean) {
		if (value) {
			this.setAttribute("bubble", "");
		}
		else {
			this.removeAttribute("bubble");
		}
	}

	get progress(): boolean {
		return this.hasAttribute("progress");
	}

	set progress(value: boolean) {
		if (value) {
			this.setAttribute("progress", "");
		}
		else {
			this.removeAttribute("progress");
		}
	}

	get min(): string {
		return this.slider.min;
	}

	set min(value: string) {
		this.slider.min = value;
		this.onInput();
	}

	get max(): string {
		return this.slider.max;
	}

	set max(value: string) {
		this.slider.max = value;
		this.onInput();
	}

	get step(): string {
		return this.slider.step;
	}

	set step(value: string) {
		this.slider.step = value;
	}

	get value(): string {
		return this.slider.value;
	}

	set value(value: string) {
		this.slider.value = value;
		this.onInput();
	}

	addInputListener(listener: () => any): void {
		this.slider.addEventListener("input", listener, false);
	}

	setValueFormatter(formatter: ValueFormatter): void {
		this.valueFormatter = formatter;
	}

	private onInput(): void {
		const value = this.slider.value;
		const range = parseFloat(this.slider.max) - parseFloat(this.slider.min);
		const valueP = ((parseFloat(value) - parseFloat(this.slider.min)) / range) * 100;

		const thumbWidth = this.thumbElement.getBoundingClientRect().width;
		const offset = thumbWidth * valueP / 100;

		if (this.progressElement) {
			this.progressElement.style.width = "calc(" + valueP + "% - " + offset + "px)";
		}
		if (this.bubbleElement) {
			const offsetBubble = offset - (thumbWidth / 2);

			this.bubbleElement.style.left = "calc(" + valueP + "% - " + offsetBubble + "px)";
			this.bubbleElement.innerHTML = this.valueFormatter ? this.valueFormatter(value) : value;
		}

		this.thumbElement.style.left = "calc(" + valueP + "% - " + offset + "px)";
	}
}

export { WebSlider };