import { WebViewElement } from "./web-view-element";

const validateSelector = (selector: string) => {
	if (selector.indexOf("-") <= 0) {
		throw new Error("At least one dash is required in the custom element name!");
	}
};

export interface ViewElementConfig {
	selector: string;
	template?: string;
	templateUrl?: string;
	styles?: string;
	styleUrls?: string[];
	useShadow?: boolean;
}

export const ViewElement = (config: ViewElementConfig) => <T extends WebViewElement>(ctor: new (...args: any[]) => T) => {
	validateSelector(config.selector);

	let content = "";

	if (config.styles) {
		content += `<style>\n${config.styles}\n</style>`;
	}
	if (config.template) {
		content += `\n${config.template}`;
	}

	const template = document.createElement("template");
	template.innerHTML = content;

	const connectedCallback = ctor.prototype.connectedCallback || function () { };
	const initialized = ctor.prototype.initialized || function () { return false };

	ctor.prototype.connectedCallback = function () {
		if (initialized.call(this)) {
			return;
		}

		this.template = config.template;

		// Attach the template to the element.
		const clone = document.importNode(template.content, true);

		if (config.useShadow) {
			this.attachShadow({ mode: "open" }).appendChild(clone);
		}
		else {
			this.appendChild(clone);
		}

		const root = config.useShadow ? this.shadowRoot : this;

		// Bind view id's to class properties.
		for (const key of Reflect.ownKeys(this)) {
			const propName = key.toString();
			const prop = root.querySelector("#" + propName);

			if (prop) {
				this[propName] = prop;
			}
		}

		connectedCallback.call(this);
	};

	customElements.define(config.selector, ctor);
}