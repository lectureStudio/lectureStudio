import { ExecutableState } from "../../utils/executable-state";
import { Observer } from "../../utils/observable";
import { View } from "./view";
import { SlideView } from "./slide.view";

interface PlayerView extends View {

	getAudioElement(): HTMLMediaElement

	getSlideView(): SlideView;

	setDuration(duration: number): void;

	setVolume(volume: number): void;

	setTime(time: number): void;

	setMuted(muted: boolean): void;

	setFullscreen(fullscreen: boolean): void;

	setMediaState(state: ExecutableState): void

	setOnPlay(observer: Observer<void>): void;

	setOnPause(observer: Observer<void>): void;

	setOnTime(observer: Observer<number>): void;

	setOnVolume(observer: Observer<number>): void;

	setOnMute(observer: Observer<boolean>): void;

	setOnFullscreen(observer: Observer<boolean>): void;

	setOnPrevious(observer: Observer<void>): void;

	setOnNext(observer: Observer<void>): void;

}

export { PlayerView };