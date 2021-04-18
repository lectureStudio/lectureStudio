import { SlidePreview } from "../../api/view/slide-pre.view";
import { ViewElement } from "../view-element";
import { RenderSurface } from "../../render/render-surface";
import { SlideRenderSurface } from "../../render/slide-render-surface";
import { Dimension } from "../../geometry/dimension";
import { SlideShape } from "../../model/shape/slide.shape";
import { SlideRenderer } from "../../render/slide.renderer";
import { Rectangle } from "../../geometry/rectangle";
import { Page } from "../../model/page";
import { WebViewElement } from "../web-view-element";

import { ArrowRenderer } from "../../render/arrow.renderer";
import { ArrowShape } from "../../model/shape/arrow.shape";
import { EllipseRenderer } from "../../render/ellipse.renderer";
import { EllipseShape } from "../../model/shape/ellipse.shape";
import { LineRenderer } from "../../render/line.renderer";
import { LineShape } from "../../model/shape/line.shape";
import { PenRenderer } from "../../render/pen.renderer";
import { PenShape } from "../../model/shape/pen.shape";
import { PointerRenderer } from "../../render/pointer.renderer";
import { PointerShape } from "../../model/shape/pointer.shape";
import { RectangleRenderer } from "../../render/rectangle.renderer";
import { RectangleShape } from "../../model/shape/rectangle.shape";
import { SelectRenderer } from "../../render/select.renderer";
import { SelectShape } from "../../model/shape/select.shape";
import { HighlighterRenderer } from "../../render/highlighter.renderer";
import { StrokeShape } from "../../model/shape/stroke.shape";
import { TextRenderer } from "../../render/text.renderer";
import { TextShape } from "../../model/shape/text.shape";
import { TextHighlightRenderer } from "../../render/text-highlight.renderer";
import { TextHighlightShape } from "../../model/shape/text-highlight.shape";
import { LatexRenderer } from "../../render/latex.renderer";
import { LatexShape } from "../../model/shape/latex.shape";

@ViewElement({
	selector: "slide-preview",
	templateUrl: "web-slide-pre.view.html",
	styleUrls: ["web-slide-pre.view.css"],
	useShadow: true
})
class WebSlidePreview extends WebViewElement implements SlidePreview {

	private slideCanvas: HTMLCanvasElement;

	private slideRenderSurface: SlideRenderSurface;

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

		this.slideRenderSurface = new SlideRenderSurface(this.slideCanvas);
		this.slideRenderSurface.setSize(size.width, size.height);
		this.slideRenderSurface.registerRenderer(SlideShape.name, new SlideRenderer());
		this.slideRenderSurface.registerRenderer(PenShape.name, new PenRenderer());
		this.slideRenderSurface.registerRenderer(StrokeShape.name, new HighlighterRenderer());
		this.slideRenderSurface.registerRenderer(PointerShape.name, new PointerRenderer());
		this.slideRenderSurface.registerRenderer(ArrowShape.name, new ArrowRenderer());
		this.slideRenderSurface.registerRenderer(RectangleShape.name, new RectangleRenderer());
		this.slideRenderSurface.registerRenderer(LineShape.name, new LineRenderer());
		this.slideRenderSurface.registerRenderer(EllipseShape.name, new EllipseRenderer());
		this.slideRenderSurface.registerRenderer(SelectShape.name, new SelectRenderer());
		this.slideRenderSurface.registerRenderer(TextShape.name, new TextRenderer());
		this.slideRenderSurface.registerRenderer(TextHighlightShape.name, new TextHighlightRenderer());
		this.slideRenderSurface.registerRenderer(LatexShape.name, new LatexRenderer());;

		this.renderSurface();
	}

	getSlideRenderSurface(): RenderSurface {
		return this.slideRenderSurface;
	}

	setPage(page: Page): void {
		this.page = page;

		if (this.slideRenderSurface) {
			this.renderSurface();
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

		this.slideRenderSurface.renderSlideShape(this.page.getSlideShape(), bounds)
			.then((imageSource: CanvasImageSource) => {
				this.slideRenderSurface.renderShapes(this.page.getShapes());
			});
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