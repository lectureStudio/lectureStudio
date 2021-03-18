import { ViewElement } from "../../view/view-element";
import { Property } from "../../utils/property";
import { WebViewElement } from "../../view/web-view-element";

@ViewElement({
	selector: "web-select",
	templateUrl: "web-select.html",
	styleUrls: ["web-select.css"]
})
class WebSelect extends WebViewElement {

	private selectedCell: HTMLElement;

	private cellList: HTMLElement;

	private readonly _value: Property<string>;

	private readonly _selectedIndex: Property<number>;


	constructor() {
		super();

		this._value = new Property();
		this._selectedIndex = new Property();
	}

	connectedCallback() {
		for (let child of Array.from(this.children)) {
			if (child instanceof WebSelectCell) {
				if (child == this.selectedCell) {
					continue;
				}

				const cellIndex = this.cellList.childElementCount;

				this.cellList.appendChild(child);

				child.addEventListener("click", () => {
					this.selectedCell.innerHTML = child.innerHTML;
					this._value.value = child.getAttribute("value");
					this._selectedIndex.value = cellIndex;
				});

				if (child.getAttribute("selected") !== null) {
					this.selectedCell.innerHTML = child.innerHTML;
				}
			}
		}

		this.selectedCell.addEventListener("click", () => {
			this.cellList.classList.toggle("web-select-list-show")
		});

		document.addEventListener("click", (event: MouseEvent) => {
			if (!this.selectedCell.contains(<Element>event.target)) {
				const dropdowns = document.querySelectorAll("#cellList");

				for (let i in dropdowns) {
					const openDropdown = dropdowns[i];

					if (openDropdown.classList && openDropdown.classList.contains("web-select-list-show")) {
						openDropdown.classList.remove("web-select-list-show");
					}
				}
			}
		});
	}

	get selectedIndex() {
		return this._selectedIndex.value;
	}

	get value() {
		return this._value.value;
	}

	get selectedIndexProperty() {
		return this._selectedIndex;
	}

	get valueProperty() {
		return this._value;
	}
}

@ViewElement({
	selector: "web-select-cell"
})
class WebSelectCell extends WebViewElement {

	constructor() {
		super();
	}

	get value(): string {
		return this.getAttribute("value");
	}

	set value(value: string) {
		this.setAttribute("value", value);
	}

	get selected(): boolean {
		return this.hasAttribute("selected");
	}

	set selected(value: boolean) {
		if (value) {
			this.setAttribute("selected", "");
		}
		else {
			this.removeAttribute("selected");
		}
	}

}

export { WebSelect, WebSelectCell };