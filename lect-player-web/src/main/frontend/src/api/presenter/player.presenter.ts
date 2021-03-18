import { Presenter } from "./presenter";
import { PlayerView } from "../view/player.view";
import { RecordingPlayer } from "../../media/recording-player";
import { View } from "../view/view";
import { FullscreenCommand } from "../../command/fullscreen.command";
import { Shortcut, KeyEventOptions } from "../../utils/shortcut";

class PlayerPresenter extends Presenter<PlayerView> {

	/** The recording player to control by the view. */
	private readonly recordingPlayer: RecordingPlayer;

	/** The shortcut manager for the attached view. */
	private readonly shortcut: Shortcut;


	constructor(view: PlayerView, parent: Presenter<View>, recordingPlayer: RecordingPlayer) {
		super(view, parent);

		this.recordingPlayer = recordingPlayer;
		this.shortcut = new Shortcut();
	}

	initialize(): void {
		this.recordingPlayer.getStateProperty().subscribe(this.view.setMediaState.bind(this.view));
		this.recordingPlayer.getDurationProperty().subscribe(this.view.setDuration.bind(this.view));
		this.recordingPlayer.getTimeProperty().subscribe(this.view.setTime.bind(this.view));
		this.recordingPlayer.getVolumeProperty().subscribe(this.view.setVolume.bind(this.view));
		this.recordingPlayer.getMutedProperty().subscribe(this.view.setMuted.bind(this.view));

		this.view.setOnPlay(this.recordingPlayer.start.bind(this.recordingPlayer));
		this.view.setOnPause(this.recordingPlayer.suspend.bind(this.recordingPlayer));
		this.view.setOnTime(this.recordingPlayer.setTime.bind(this.recordingPlayer));
		this.view.setOnVolume(this.recordingPlayer.setVolume.bind(this.recordingPlayer));
		this.view.setOnMute(this.recordingPlayer.setMuted.bind(this.recordingPlayer));
		this.view.setOnPrevious(this.recordingPlayer.previous.bind(this.recordingPlayer));
		this.view.setOnNext(this.recordingPlayer.next.bind(this.recordingPlayer));

		(this.view as any).addEventListener("click", () => {
			if (this.recordingPlayer.started()) {
				//this.recordingPlayer.suspend();
			}
		});

		this.view.setOnFullscreen((value: boolean) => {
			this.execute(new FullscreenCommand(value));
		});

		// Add keyboard shortcuts.
		this.bindShortcuts();

		// Update view.
		if (this.recordingPlayer.getDuration()) {
			this.view.setDuration(this.recordingPlayer.getDuration());
		}

		this.view.setVolume(this.recordingPlayer.getVolume());
		this.view.setMuted(this.recordingPlayer.getMuted());

		this.recordingPlayer.init();
	}

	destroy(): void {
		// Unbind all shortcuts.
		this.shortcut.dispose();

		super.destroy();
	}

	private bindShortcuts(): void {
		const previousShortcut = () => this.recordingPlayer.previous();
		const nextShortcut = () => this.recordingPlayer.next();
		const playPauseShortcut = () => {
			if (!this.recordingPlayer.started()) {
				this.recordingPlayer.start()
			}
			else if (this.recordingPlayer.started()) {
				this.recordingPlayer.suspend()
			}
		};
		const muteShortcut = () => this.recordingPlayer.setMuted(!this.recordingPlayer.getMuted());
		const fullscreenShortcut = () => this.execute(new FullscreenCommand());

		// When the key is held down.
		const repeatEventOptions: KeyEventOptions = {
			repeat: true
		};

		this.shortcut.bind(["arrowUp", "arrowLeft", "pageUp"], previousShortcut, repeatEventOptions);
		this.shortcut.bind(["arrowDown", "arrowRight", "pageDown"], nextShortcut, repeatEventOptions);
		this.shortcut.bind("space", playPauseShortcut);
		this.shortcut.bind("m", muteShortcut);
		this.shortcut.bind("f", fullscreenShortcut);
	}
}

export { PlayerPresenter };