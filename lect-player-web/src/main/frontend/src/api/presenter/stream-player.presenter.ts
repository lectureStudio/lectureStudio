import { Presenter } from "./presenter";
import { PlayerView } from "../view/player.view";
import { View } from "../view/view";
import { FullscreenCommand } from "../../command/fullscreen.command";
import { Shortcut } from "../../utils/shortcut";
import { StreamPlayer } from "../../media/stream-player";

class StreamPlayerPresenter extends Presenter<PlayerView> {

	/** The stream player to control by the view. */
	private readonly streamPlayer: StreamPlayer;

	/** The shortcut manager for the attached view. */
	private readonly shortcut: Shortcut;


	constructor(view: PlayerView, parent: Presenter<View>, streamPlayer: StreamPlayer) {
		super(view, parent);

		this.streamPlayer = streamPlayer;
		this.shortcut = new Shortcut();
	}

	initialize(): void {
		this.streamPlayer.getStateProperty().subscribe(this.view.setMediaState.bind(this.view));
		this.streamPlayer.getVolumeProperty().subscribe(this.view.setVolume.bind(this.view));
		this.streamPlayer.getMutedProperty().subscribe(this.view.setMuted.bind(this.view));

		this.view.setOnPlay(this.streamPlayer.start.bind(this.streamPlayer));
		this.view.setOnPause(this.streamPlayer.suspend.bind(this.streamPlayer));
		this.view.setOnVolume(this.streamPlayer.setVolume.bind(this.streamPlayer));
		this.view.setOnMute(this.streamPlayer.setMuted.bind(this.streamPlayer));

		this.view.setOnFullscreen((value: boolean) => {
			this.execute(new FullscreenCommand(value));
		});

		// Add keyboard shortcuts.
		this.bindShortcuts();

		// Update view.
		this.view.setVolume(this.streamPlayer.getVolume());
		this.view.setMuted(this.streamPlayer.getMuted());

		this.streamPlayer.init();
		this.streamPlayer.start();
	}

	destroy(): void {
		// Unbind all shortcuts.
		this.shortcut.dispose();

		super.destroy();
	}

	private bindShortcuts(): void {
		const muteShortcut = () => this.streamPlayer.setMuted(!this.streamPlayer.getMuted());
		const fullscreenShortcut = () => this.execute(new FullscreenCommand());

		this.shortcut.bind("m", muteShortcut);
		this.shortcut.bind("f", fullscreenShortcut);
	}
}

export { StreamPlayerPresenter };