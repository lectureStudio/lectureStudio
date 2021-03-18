import { Executable } from "../utils/executable";
import { ExecutableState } from "../utils/executable-state";

class MediaPlayer extends Executable {

	private media: HTMLMediaElement;


	constructor(media: HTMLMediaElement) {
		super();

		this.media = media;

		this.media.addEventListener("pause", this.mediaPaused.bind(this), false);
		this.media.addEventListener("play", this.mediaPlaying.bind(this), false);
		this.media.addEventListener("ended", this.mediaEnded.bind(this), false);
		this.media.addEventListener("error", this.mediaError.bind(this), false);
	}

	set source(source: string) {
		this.media.src = source
	}

	get duration(): number {
		return this.media.duration;
	}

	set time(time: number) {
		this.media.currentTime = time;
	}

	get time(): number {
		return this.media.currentTime;
	}

	set muted(muted: boolean) {
		this.media.muted = muted;
	}

	get muted(): boolean {
		return this.media.muted;
	}

	set volume(volume: number) {
		this.media.volume = volume;
	}

	get volume(): number {
		return this.media.volume;
	}

	init(): void {
		this.setState(ExecutableState.Initializing);
		this.setState(ExecutableState.Initialized);
	}

	start(): void {
		if (this.created() || this.destroyed()) {
			this.init();
		}

		this.setState(ExecutableState.Starting);

		this.media.play();
	}

	stop(): void {
		this.setState(ExecutableState.Stopping);

		this.media.pause();
	}

	suspend(): void {
		this.setState(ExecutableState.Suspending);

		this.media.pause();
	}

	destroy(): void {
		if (this.started() || this.suspended()) {
			stop();
		}

		this.setState(ExecutableState.Destroying);
		this.setState(ExecutableState.Destroyed);
	}

	addDurationListener(listener: (duration: number) => void): void {
		this.media.addEventListener("durationchange", () => {
			listener(this.media.duration);
		}, false);
	}

	addTimeListener(listener: (time: number) => void): void {
		this.media.addEventListener("timeupdate", () => {
			listener(this.media.currentTime);
		}, false);
	}

	addVolumeListener(listener: (volume: number) => void): void {
		this.media.addEventListener("volumechange", () => {
			listener(this.media.volume);
		}, false);
	}

	addMutedListener(listener: (muted: boolean) => void): void {
		this.media.addEventListener("volumechange", () => {
			listener(this.media.muted);
		}, false);
	}

	private mediaPaused(): void {
		if (this.started()) {
			// Strict state change.
			this.setState(ExecutableState.Suspending);
		}
		this.setState(ExecutableState.Suspended);
	}

	private mediaPlaying(): void {
		if (this.suspended() || this.stopped()) {
			this.setState(ExecutableState.Starting);
		}
		this.setState(ExecutableState.Started);
	}

	private mediaEnded(): void {
		if (this.initialized() || this.stopped()) {
			return;
		}

		if (this.started() || this.suspended()) {
			this.setState(ExecutableState.Stopping);
		}

		this.time = 0;

		this.setState(ExecutableState.Stopped);
	}

	private mediaError(): void {
		if (this.media.error) {
			let error = "Media error code: " + this.media.error.code;

			if (this.media.error.message) {
				error += ", message: " + this.media.error.message;
			}

			console.error(error);
		}

		this.setState(ExecutableState.Error);
	}
}

export { MediaPlayer };