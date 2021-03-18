import { View } from "./view";
import { RenderSurface } from "../../render/render-surface";
import { Page } from "../../model/page";

interface SlidePreview extends View {

	getSlideRenderSurface(): RenderSurface;

	setSize(width: number, height: number): void;

	setPage(page: Page): void;

}

export { SlidePreview };