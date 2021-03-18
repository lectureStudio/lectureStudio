import { SlideView } from "../../api/view/slide.view";
import { SlideRenderSurface } from "../../render/slide-render-surface";
import { RenderSurface } from "../../render/render-surface";
import { TextLayerSurface } from "../../render/text-layer-surface";
import { ViewElement } from "../view-element";
import { WebViewElement } from "../web-view-element";

@ViewElement({
	selector: "slide-view",
	templateUrl: "web-slide.view.html",
	styleUrls: ["web-slide.view.css"]
})
class WebSlideView extends WebViewElement implements SlideView {

	private slideRenderSurface: SlideRenderSurface;

	private actionRenderSurface: RenderSurface;

	private volatileRenderSurface: RenderSurface;

	private textLayerSurface: TextLayerSurface;
	

	constructor() {
		super();
	}

	connectedCallback() {
		const slideCanvas = this.querySelector<HTMLCanvasElement>('.slide-canvas');
		const actionCanvas = this.querySelector<HTMLCanvasElement>('.action-canvas');
		const volatileCanvas = this.querySelector<HTMLCanvasElement>('.volatile-canvas');
		const textLayer = this.querySelector<HTMLElement>('.text-layer');

		this.slideRenderSurface = new SlideRenderSurface(slideCanvas);
		this.actionRenderSurface = new RenderSurface(actionCanvas);
		this.volatileRenderSurface = new RenderSurface(volatileCanvas);
		this.textLayerSurface = new TextLayerSurface(textLayer);

		this.resize();

		window.addEventListener('resize', this.resize.bind(this), false);
	}

	getActionRenderSurface(): RenderSurface {
		return this.actionRenderSurface;
	}

	getSlideRenderSurface(): SlideRenderSurface {
		return this.slideRenderSurface;
	}

	getVolatileRenderSurface(): RenderSurface {
		return this.volatileRenderSurface;
	}

	getTextLayerSurface(): TextLayerSurface {
		return this.textLayerSurface;
	}

	repaint(): void {
		this.resize();
	}

	private resize() {
		const slideView = this.querySelector('.slide-view');
		const slideRatio = 4 / 3;
		let width = slideView.clientWidth;
		let height = slideView.clientHeight;
		const viewRatio = width / height;

		if (viewRatio > slideRatio) {
			width = height * slideRatio;
		}
		else {
			height = width / slideRatio;
		}

		this.slideRenderSurface.setSize(width, height);
		this.actionRenderSurface.setSize(width, height);
		this.volatileRenderSurface.setSize(width, height);
		this.textLayerSurface.setSize(width, height);
	}
}

export { WebSlideView };