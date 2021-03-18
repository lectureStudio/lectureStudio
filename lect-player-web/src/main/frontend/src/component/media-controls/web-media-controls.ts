import { Observer } from "../../utils/observable";
import { ExecutableState } from "../../utils/executable-state";
import { ViewElement } from "../../view/view-element";
import { WebSelect } from "../../component/select/web-select";
import { WebSlider } from "../slider/web-slider";
import { WebViewElement } from "../../view/web-view-element";

@ViewElement({
	selector: "web-media-controls",
	templateUrl: "web-media-controls.html",
	styleUrls: ["web-media-controls.css"]
})
class WebMediaControls extends WebViewElement {

	private audio: HTMLMediaElement;

	private prevButton: HTMLElement;

	private nextButton: HTMLElement;

	private playButton: HTMLElement;

	private pauseButton: HTMLElement;

	private playbackSpeedSelect: WebSelect;

	private fullscreenButton: HTMLElement;

	private timeSlider: WebSlider;

	private volumeSlider: WebSlider;

	private volumeIndicator: HTMLElement;

	private currentTimeText: HTMLElement;

	private durationText: HTMLElement;

	private duration: number;

	private volume: number;


	constructor() {
		super();
	}

	connectedCallback() {
		this.timeSlider.setValueFormatter(value => this.formatTime(parseFloat(value) * this.duration));

		document.addEventListener("fullscreenchange", () => {
			this.setFullscreen((document as any).fullscreenElement != null);
		});

		this.querySelectorAll(".rate-steps > li").forEach((option: HTMLElement) => {
			option.addEventListener('click', () => {
				if (!this.classList.contains('active')) {
					option.parentNode.querySelector('li.active').classList.remove('active');
					option.classList.add('active');

					this.audio.playbackRate = parseFloat(option.dataset.value);
				}
			});
		});
	}

	static get observedAttributes() {
		return ["visible"];
	}

	get visible(): boolean {
		return this.hasAttribute("visible");
	}

	set visible(value: boolean) {
		if (value) {
			this.setAttribute("visible", "");
		}
		else {
			this.removeAttribute("visible");
		}
	}

	show(): void {
		this.visible = true;
	}

	hide(): void {
		this.visible = false;
	}

	getAudioElement(): HTMLMediaElement {
		return this.audio;
	}

	setDuration(duration: number): void {
		this.duration = duration;
		this.durationText.innerHTML = this.formatTime(duration);
	}

	setVolume(volume: number): void {
		this.volume = volume;

		if (volume === 0) {
			this.setVolumeIndicator("icon-volume0");
		}
		else if (volume < 0.33) {
			this.setVolumeIndicator("icon-volume1");
		}
		else if (volume > 0.33 && volume < 0.66) {
			this.setVolumeIndicator("icon-volume2");
		}
		else if (volume > 0.66) {
			this.setVolumeIndicator("icon-volume3");
		}

		this.volumeSlider.value = (volume * 100).toString();
	}

	setTime(time: number): void {
		this.currentTimeText.innerHTML = this.formatTime(time);
		this.timeSlider.value = (time / this.duration).toString();
	}

	setMuted(muted: boolean): void {
		if (muted) {
			this.setVolumeIndicator("icon-mute");
		}
		else {
			this.setVolume(this.volume);
		}
	}

	setFullscreen(fullscreen: boolean): void {
		if (fullscreen) {
			this.setFullscreenIndicator("icon-contract");
		}
		else {
			this.setFullscreenIndicator("icon-expand");
		}
	}

	setMediaState(state: ExecutableState): void {
		const started = state === ExecutableState.Started;

		this.playButton.style.display = started ? "none" : "inherit";
		this.pauseButton.style.display = started ? "inherit" : "none";
	}

	setOnPlay(observer: Observer<void>): void {
		this.playButton.addEventListener("click", (event: Event) => {
			event.stopPropagation();
			observer();
		}, false);
	}

	setOnPause(observer: Observer<void>): void {
		this.pauseButton.addEventListener("click", () => {
			event.stopPropagation();
			observer();
		}, false);
	}

	setOnTime(observer: Observer<number>): void {
		this.timeSlider.addInputListener(() => {
			observer(parseFloat(this.timeSlider.value));
		});
	}

	setOnVolume(observer: Observer<number>): void {
		this.volumeSlider.addInputListener(() => {
			observer(parseFloat(this.volumeSlider.value) / 100);
		});
	}

	setOnMute(observer: Observer<boolean>): void {
		this.volumeIndicator.addEventListener("click", () => {
			event.stopPropagation();
			observer(!this.volumeIndicator.classList.contains("icon-mute"));
		}, false);
	}

	setOnPlaybackSpeed(observer: Observer<string>): void {
		this.playbackSpeedSelect.valueProperty.subscribe(observer);
	}

	setOnFullscreen(observer: Observer<boolean>): void {
		this.fullscreenButton.addEventListener("click", () => {
			event.stopPropagation();
			observer(this.fullscreenButton.classList.contains("icon-expand"));
		}, false);
	}

	setOnPrevious(observer: Observer<void>): void {
		this.prevButton.addEventListener("click", () => {
			event.stopPropagation();
			observer();
		}, false);
	}

	setOnNext(observer: Observer<void>): void {
		this.nextButton.addEventListener("click", () => {
			event.stopPropagation();
			observer();
		}, false);
	}

	private formatTime(time: number): string {
		const seconds = Math.floor(time % 60);
		time /= 60;
		const minutes = Math.floor(time % 60);
		time /= 60;
		const hours = Math.floor(time);

		if (hours > 0) {
			return String.format("%01d:%02d:%02d", hours, minutes, seconds);
		}

		const minuteFormat = minutes > 9 ? "%02d" : "%d";

		return String.format(minuteFormat + ":%02d", minutes, seconds);
	}

	private setFullscreenIndicator(indicatorName: string): void {
		const classList = this.fullscreenButton.classList;

		classList.remove("icon-expand", "icon-contract");
		classList.add(indicatorName);
	}

	private setVolumeIndicator(indicatorName: string): void {
		const classList = this.volumeIndicator.classList;

		classList.remove("icon-mute");

		for (let i = 0; i < 4; i++) {
			classList.remove("icon-volume" + i);
		}

		classList.add(indicatorName);
	}
}

export { WebMediaControls };