import { ExecutableBase } from "../utils/executable-base";
import { MediaPlayer } from "./media-player";
import { ActionPlayer } from "../action/action-player";
import { PlaybackModel } from "../model/playback-model";
import { ExecutableState } from "../utils/executable-state";
import { SlideDocument } from "../model/document";
import { Property } from "../utils/property";
import { ClassroomMediaClient } from "../net/classroom-media-client";

class StreamPlayer extends ExecutableBase {

	private readonly mediaPlayer: MediaPlayer;

	private readonly actionPlayer: ActionPlayer;

	private readonly playbackModel: PlaybackModel;

	private readonly mediaClient: ClassroomMediaClient;


	constructor(mediaClient: ClassroomMediaClient, mediaPlayer: MediaPlayer, actionPlayer: ActionPlayer) {
		super();

		this.mediaClient = mediaClient;
		this.mediaPlayer = mediaPlayer;
		this.actionPlayer = actionPlayer;
		this.playbackModel = new PlaybackModel();
		this.playbackModel.selectedPageIndex = 0;

		this.actionPlayer.getExecutor().setOnSelectPageIndex(this.updateSelectedPage.bind(this));

		this.mediaPlayer.addDurationListener(this.playbackModel.setDuration.bind(this.playbackModel));
		this.mediaPlayer.addTimeListener(this.playbackModel.setTime.bind(this.playbackModel));
		this.mediaPlayer.addVolumeListener(this.playbackModel.setVolume.bind(this.playbackModel));
		this.mediaPlayer.addMutedListener(this.playbackModel.setMuted.bind(this.playbackModel));
	}

	previous(): void {
		
	}

	next(): void {
		
	}

	getDocument(): SlideDocument {
		return this.actionPlayer.getDocument();
	}

	getDuration(): number {
		return 0;
	}

	setTime(time: number): void {
		
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
		this.mediaClient.init();
	}

	protected startInternal(): void {
		this.mediaClient.start();
	}

	protected stopInternal(): void {
		this.mediaClient.stop();
	}

	protected suspendInternal(): void {
		this.mediaClient.suspend();
	}

	protected destroyInternal(): void {
		this.mediaClient.destroy();
	}

	private updateSelectedPage(index: number) {
		this.playbackModel.selectedPageIndex = index;
	}

}

export { StreamPlayer };