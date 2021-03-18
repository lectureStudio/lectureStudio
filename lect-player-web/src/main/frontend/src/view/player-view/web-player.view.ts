import { PlayerView } from "../../api/view/player.view";
import { SlideView } from "../../api/view/slide.view";
import { WebMediaControls } from "../../component/media-controls/web-media-controls";
import { Observer } from "../../utils/observable";
import { ExecutableState } from "../../utils/executable-state";
import { WebSlideView } from "../slide-view/web-slide.view";
import { ViewElement } from "../view-element";
import { WebViewElement } from "../web-view-element";

@ViewElement({
	selector: "player-view",
	templateUrl: "web-player.view.html",
	styleUrls: ["web-player.view.css"]
})
class WebPlayerView extends WebViewElement implements PlayerView {

	private slideView: WebSlideView;

	private mediaControls: WebMediaControls;

	private idleHandle: number;

	private idleTime: number;

	private resetIdleHandler: () => void;

	private showControlsHandler: () => void;


	constructor() {
		super();
	}

	connectedCallback() {
		this.resetIdleHandler = this.resetIdle.bind(this);
		this.showControlsHandler = () => {
			this.showControls();
			this.initHideControls();
		};

		this.mediaControls.show();
		this.slideView.repaint();
		this.setVolume(0);
	}

	getAudioElement(): HTMLMediaElement {
		return this.mediaControls.getAudioElement();
	}

	getSlideView(): SlideView {
		return this.slideView;
	}

	setDuration(duration: number): void {
		this.mediaControls.setDuration(duration);
	}

	setVolume(volume: number): void {
		this.mediaControls.setVolume(volume);
	}

	setTime(time: number): void {
		this.mediaControls.setTime(time);
	}

	setMuted(muted: boolean): void {
		this.mediaControls.setMuted(muted);
	}

	setFullscreen(fullscreen: boolean): void {
		this.mediaControls.setFullscreen(fullscreen);
	}

	setMediaState(state: ExecutableState): void {
		const started = state === ExecutableState.Started;

		if (started) {
			this.initHideControls();
		}
		else {
			this.shutdownIdleHandler();
			this.showControls();
		}

		this.mediaControls.setMediaState(state);
	}

	setOnPlay(observer: Observer<void>): void {
		this.mediaControls.setOnPlay(observer);
	}

	setOnPause(observer: Observer<void>): void {
		this.mediaControls.setOnPause(observer);
	}

	setOnTime(observer: Observer<number>): void {
		this.mediaControls.setOnTime(observer);
	}

	setOnVolume(observer: Observer<number>): void {
		this.mediaControls.setOnVolume(observer);
	}

	setOnMute(observer: Observer<boolean>): void {
		this.mediaControls.setOnMute(observer);
	}

	setOnFullscreen(observer: Observer<boolean>): void {
		this.mediaControls.setOnFullscreen(observer);
	}

	setOnPrevious(observer: Observer<void>): void {
		this.mediaControls.setOnPrevious(observer);
	}

	setOnNext(observer: Observer<void>): void {
		this.mediaControls.setOnNext(observer);
	}

	private showControls(): void {
		this.removeEventListener("mousemove", this.showControlsHandler);

		this.mediaControls.show();
	}

	private initHideControls(): void {
		if (this.idleHandle) {
			return;
		}

		this.idleHandle = window.setInterval(this.handleIdle.bind(this), 1000);
		this.idleTime = Date.now();

		this.addEventListener("mousemove", this.resetIdleHandler);
	}

	private resetIdle(): void {
		this.idleTime = Date.now();
	}

	private handleIdle(): void {
		if (Date.now() - this.idleTime > 3000) {
			this.shutdownIdleHandler();

			this.mediaControls.hide();

			this.removeEventListener("mousemove", this.resetIdleHandler);
			this.addEventListener("mousemove", this.showControlsHandler);
		}
	}

	private shutdownIdleHandler(): void {
		if (this.idleHandle) {
			window.clearInterval(this.idleHandle);
			this.idleHandle = null;
		}
	}
}

export { WebPlayerView };