import { Property } from "../utils/property";
import { ExecutableState } from "../utils/executable-state";

class PlaybackModel {

	private readonly _state = new Property<ExecutableState>();

	private readonly _selectedPageIndex = new Property<number>();

	private readonly _duration = new Property<number>();

	private readonly _time = new Property<number>();

	private readonly _volume = new Property<number>();

	private readonly _muted = new Property<boolean>();


	get selectedPageIndexProperty() {
		return this._selectedPageIndex;
	}

	get selectedPageIndex() {
		return this._selectedPageIndex.value;
	}

	set selectedPageIndex(index: number) {
		this._selectedPageIndex.value = index;
	}

	get stateProperty() {
		return this._state;
	}

	getState(): ExecutableState {
		return this._state.value;
	}

	setState(state: ExecutableState): void {
		this._state.value = state;
	}

	get durationProperty() {
		return this._duration;
	}

	getDuration(): number {
		return this._duration.value;
	}

	setDuration(duration: number): void {
		this._duration.value = duration;
	}

	get timeProperty() {
		return this._time;
	}

	getTime(): number {
		return this._time.value;
	}

	setTime(time: number): void {
		this._time.value = time;
	}

	get volumeProperty() {
		return this._volume;
	}

	getVolume(): number {
		return this._volume.value;
	}

	setVolume(volume: number): void {
		this._volume.value = volume;
	}

	get mutedProperty() {
		return this._muted;
	}

	getMuted(): boolean {
		return this._muted.value;
	}

	setMuted(muted: boolean): void {
		this._muted.value = muted;
	}
}

export { PlaybackModel };