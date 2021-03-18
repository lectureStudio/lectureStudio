import { SlidePreview } from "../../api/view/slide-pre.view";
import { ViewElement } from "../view-element";
import { RenderSurface } from "../../render/render-surface";
import { Dimension } from "../../geometry/dimension";
import { SlideShape } from "../../model/shape/slide.shape";
import { SlideRenderer } from "../../render/slide.renderer";
import { Rectangle } from "../../geometry/rectangle";
import { Page } from "../../model/page";
import { WebViewElement } from "../web-view-element";

@ViewElement({
	selector: "slide-preview",
	templateUrl: "web-slide-pre.view.html",
	styleUrls: ["web-slide-pre.view.css"],
	useShadow: true
})
class WebSlidePreview extends WebViewElement implements SlidePreview {

	private slideCanvas: HTMLCanvasElement;

	private slideRenderSurface: RenderSurface;

	private page: Page;


	constructor() {
		super();
	}

	initialized(): boolean {
		return this.slideRenderSurface != null;
	}

	connectedCallback() {
		this.slideCanvas = this.shadowRoot.querySelector('.slide-canvas');

		const size = this.getSize();

		this.style.margin = "4px 4px 0 4px"; // Edge fix
		this.style.width = size.width + "px";
		this.style.height = size.height + "px";

		this.slideRenderSurface = new RenderSurface(this.slideCanvas);
		this.slideRenderSurface.setSize(size.width, size.height);
		this.slideRenderSurface.registerRenderer(SlideShape.name, new SlideRenderer());

		this.renderSurface();
	}

	getSlideRenderSurface(): RenderSurface {
		return this.slideRenderSurface;
	}

	setPage(page: Page): void {
		this.page = page;

		if (this.slideRenderSurface) {
			const size = this.slideRenderSurface.getSize();
			const bounds = new Rectangle(0, 0, size.width, size.height);

			this.slideRenderSurface.renderShape(page.getSlideShape(), bounds);
		}
	}

	setSize(width: number, height: number): void {
		if (this.slideRenderSurface) {
			this.slideRenderSurface.setSize(width, height);

			this.renderSurface();
		}
	}

	private renderSurface(): void {
		if (!this.page) {
			return;
		}

		const size = this.slideRenderSurface.getSize();
		const bounds = new Rectangle(0, 0, size.width, size.height);

		this.slideRenderSurface.renderShape(this.page.getSlideShape(), bounds);
	}

	private getSize(): Dimension {
		const slideRatio = 4 / 3;
		const parentBounds = this.parentElement.getBoundingClientRect();
		let width = parentBounds.width - this.getScrollbarWidth() - 8;
		let height = parentBounds.height;
		const viewRatio = width / height;

		if (viewRatio > slideRatio) {
			width = height * slideRatio;
		}
		else {
			height = width / slideRatio;
		}

		return new Dimension(width, height);
	}

	private getScrollbarWidth(): number {
		const outer = document.createElement("div");
		outer.style.visibility = "hidden";
		outer.style.width = "100px";
		document.body.appendChild(outer);

		const widthNoScroll = outer.offsetWidth;
		outer.style.overflow = "scroll";

		const inner = document.createElement("div");
		inner.style.width = "100%";
		outer.appendChild(inner);

		const widthWithScroll = inner.offsetWidth;

		outer.parentNode.removeChild(outer);

		return widthNoScroll - widthWithScroll;
	}
}

export { WebSlidePreview };