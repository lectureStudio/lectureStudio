import { MediaPlayer } from "./media-player";
import { ActionPlayer } from "../action/action-player";
import { ExecutableBase } from "../utils/executable-base";
import { ExecutableState } from "../utils/executable-state";
import { PlaybackModel } from "../model/playback-model";
import { Property } from "../utils/property";
import { SlideDocument } from "../model/document";

class RecordingPlayer extends ExecutableBase {

	private readonly mediaPlayer: MediaPlayer;

	private readonly actionPlayer: ActionPlayer;

	private readonly playbackModel: PlaybackModel;

	private onMediaStateCallback: (state: ExecutableState) => void;


	constructor(mediaPlayer: MediaPlayer, actionPlayer: ActionPlayer) {
		super();

		this.mediaPlayer = mediaPlayer;
		this.actionPlayer = actionPlayer;
		this.playbackModel = new PlaybackModel();
		this.playbackModel.selectedPageIndex = 0;

		this.onMediaStateCallback = this.onMediaState.bind(this);

		this.actionPlayer.getExecutor().setOnSelectPageIndex(this.updateSelectedPage.bind(this));

		this.mediaPlayer.addStateListener(this.onMediaStateCallback);
		this.mediaPlayer.addDurationListener(this.playbackModel.setDuration.bind(this.playbackModel));
		this.mediaPlayer.addTimeListener(this.playbackModel.setTime.bind(this.playbackModel));
		this.mediaPlayer.addVolumeListener(this.playbackModel.setVolume.bind(this.playbackModel));
		this.mediaPlayer.addMutedListener(this.playbackModel.setMuted.bind(this.playbackModel));
	}

	previous(): void {
		this.setSelectedPage(this.playbackModel.selectedPageIndex - 1);
	}

	next(): void {
		this.setSelectedPage(this.playbackModel.selectedPageIndex + 1);
	}

	setSelectedPage(pageNumber: number) {
		const doc = this.getDocument();

		if (pageNumber < 0 || pageNumber > doc.getPageCount() - 1) {
			return;
		}

		const timestamp = this.actionPlayer.seekByPage(pageNumber);

		if (timestamp > -1) {
			this.mediaPlayer.time = timestamp / 1000;

			this.updateSelectedPage(pageNumber);
		}
	}

	getDocument(): SlideDocument {
		return this.actionPlayer.getDocument();
	}

	getDuration(): number {
		return this.mediaPlayer.duration;
	}

	setTime(time: number): void {
		time = time * this.mediaPlayer.duration;

		this.actionPlayer.seekByTime(time * 1000);
		this.mediaPlayer.time = time;
	}

	getTime(): number {
		return this.mediaPlayer.time;
	}

	setMuted(muted: boolean): void {
		this.mediaPlayer.muted = muted;
	}

	getMuted(): boolean {
		return this.mediaPlayer.muted;
	}

	setVolume(volume: number): void {
		if (this.mediaPlayer.muted) {
			this.mediaPlayer.muted = false;
		}
		this.mediaPlayer.volume = volume;
	}

	getVolume(): number {
		return this.mediaPlayer.volume;
	}

	getSelectedPageProperty(): Property<number> {
		return this.playbackModel.selectedPageIndexProperty;
	}

	getStateProperty(): Property<ExecutableState> {
		return this.playbackModel.stateProperty;
	}

	getDurationProperty(): Property<number> {
		return this.playbackModel.durationProperty;
	}

	getTimeProperty(): Property<number> {
		return this.playbackModel.timeProperty;
	}

	getVolumeProperty(): Property<number> {
		return this.playbackModel.volumeProperty;
	}

	getMutedProperty(): Property<boolean> {
		return this.playbackModel.mutedProperty;
	}

	protected initInternal(): void {
		this.mediaPlayer.init();
	}

	protected startInternal(): void {
		this.mediaPlayer.start();
	}

	protected stopInternal(): void {
		if (!this.mediaPlayer.stopped()) {
			this.mediaPlayer.stop();
		}
	}

	protected suspendInternal(): void {
		this.mediaPlayer.suspend();
	}

	protected destroyInternal(): void {
		this.mediaPlayer.removeStateListener(this.onMediaStateCallback);
		//this.mediaPlayer.removeDurationListener(this.playbackModel.setDuration);
		//this.mediaPlayer.removeTimeListener(this.playbackModel.setTime);
		//this.mediaPlayer.removeVolumeListener(this.playbackModel.setVolume);
		//this.mediaPlayer.removeMutedListener(this.playbackModel.setMuted.bind(this.playbackModel));
		this.mediaPlayer.destroy();

		this.onMediaStateCallback = null;
	}

	private updateSelectedPage(index: number) {
		this.playbackModel.selectedPageIndex = index;
	}

	private onMediaState(state: ExecutableState): void {
		switch (state) {
			case ExecutableState.Initialized:
				this.actionPlayer.init();
				break;
			case ExecutableState.Started:
				this.actionPlayer.start();
				break;
			case ExecutableState.Stopped:
				this.actionPlayer.stop();

				// Stop recording
				this.stop();
				break;
			case ExecutableState.Suspended:
				this.actionPlayer.suspend();
				break;
		}

		this.playbackModel.setState(state);
	}

}

export { RecordingPlayer };