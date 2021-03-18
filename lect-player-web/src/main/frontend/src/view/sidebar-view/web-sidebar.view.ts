import { ViewElement } from "../view-element";
import { Observer } from "../../utils/observable";
import { SidebarView } from "../../api/view/sidebar.view";
import { SlideDocument } from "../../model/document";
import { Page } from "../../model/page";
import { WebSlidePreview } from "../slide-preview/web-slide-pre.view";
import { WebViewElement } from "../web-view-element";

@ViewElement({
	selector: "sidebar-view",
	templateUrl: "web-sidebar.view.html",
	styleUrls: ["web-sidebar.view.css"],
	useShadow: true
})
class WebSidebarView extends WebViewElement implements SidebarView {

	private readonly previewPool = new Array<WebSlidePreview>();

	private previewScroller: any;

	private onPageSelect: Observer<number>;

	private lastSelectedIndex: number;
	private selectedIndex: number;


	constructor() {
		super();
	}

	initialized(): boolean {
		return this.previewScroller != null;
	}

	connectedCallback() {
		this.previewScroller.addEventListener("rangechange", (event: any) => {
			if (this.selectedIndex >= event.first || this.selectedIndex <= event.last) {
				this.setSelectedPreview(this.lastSelectedIndex, this.selectedIndex);
			}
		});
		this.previewScroller.createElement = (page: Page, index: number) => {
			const element = this.previewPool.pop();
			if (element) {
				return element;
			}

			const slideView = new WebSlidePreview();
			slideView.addEventListener("click", this.previewSelected.bind(this));

			return slideView;
		};
		this.previewScroller.updateElement = (slideView: WebSlidePreview, page: Page, index: number) => {
			slideView.id = "page" + index;
			slideView.dataset.id = index.toString();
			slideView.classList.remove("selected");
			slideView.setPage(page);
		};
		this.previewScroller.recycleElement = (slideView: WebSlidePreview, page: Page, index: number) => {
			slideView.id = null;
			slideView.dataset.id = null;

			this.previewPool.push(slideView);
		};
	}

	setDocument(doc: SlideDocument): void {
		const count = doc.getPageCount();
		const pages = new Array<Page>(count);

		for (let i = 0; i < count; i++) {
			pages[i] = doc.getPage(i);
		}

		this.previewScroller.itemSource = pages;
	}

	setSelectedPageIndex(lastIndex: number, newIndex: number): void {
		this.lastSelectedIndex = this.selectedIndex;
		this.selectedIndex = newIndex;

		this.setSelectedPreview(lastIndex, newIndex);

		this.previewScroller.scrollToIndex(newIndex);
	}

	setOnPageSelect(observer: Observer<number>): void {
		this.onPageSelect = observer;
	}

	private previewSelected(event: Event) {
		const target = event.currentTarget;

		if (target instanceof HTMLElement && this.onPageSelect) {
			const index = parseInt(target.dataset.id);
			this.onPageSelect(index);
		}
	}

	private setSelectedPreview(lastIndex: number, newIndex: number): void {
		let element = this.shadowRoot.querySelector("#page" + lastIndex);
		if (element) {
			element.classList.remove("selected");
		}

		element = this.shadowRoot.querySelector("#page" + newIndex);
		if (element) {
			element.classList.add("selected");
		}
	}
}

export { WebSidebarView };