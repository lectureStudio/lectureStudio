import { Presenter } from "./presenter";
import { PlaybackView } from "../view/playback.view";
import { ToolbarPresenter } from "./toolbar.presenter";
import { Side } from "../../utils/side";
import { SidebarPresenter } from "./sidebar.presenter";
import { PlayerPresenter } from "./player.presenter";
import { View } from "../view/view";
import { SearchService } from "../../service/search-service";
import { RenderController } from "../../render/render-controller";
import { MediaPlayer } from "../../media/media-player";
import { SyncState } from "../../utils/sync-state";
import { FileActionPlayer } from "../../action/file-action-player";
import { RecordingPlayer } from "../../media/recording-player";
import { Recording } from "../../model/recording";
import { Command } from "../../command/command";

class PlaybackPresenter extends Presenter<PlaybackView> {

	private readonly recording: Recording;


	constructor(view: PlaybackView, parent: Presenter<View>, recording: Recording) {
		super(view, parent);

		this.recording = recording;
	}

	initialize(): void {
		this.addCommandExecuter("SidebarPositionCommand", this.executeSidebarPosition);

		const toolbarView = this.view.getToolbarView();
		const playerView = this.view.getPlayerView();
		const sidebarView = this.view.getSidebarView();

		const searchService = new SearchService(this.recording.document);

		const slideView = playerView.getSlideView();

		const renderController = new RenderController();
		renderController.setActionRenderSurface(slideView.getActionRenderSurface());
		renderController.setSlideRenderSurface(slideView.getSlideRenderSurface());
		renderController.setVolatileRenderSurface(slideView.getVolatileRenderSurface());
		renderController.setTextLayerSurface(slideView.getTextLayerSurface());

		const mediaPlayer = new MediaPlayer(playerView.getAudioElement());

		// Create a blob with type information, otherwise Safari for iOS won't accept the source.
		mediaPlayer.source = URL.createObjectURL(new Blob([this.recording.audio], { type: 'audio/wav' }));

		const syncState = new SyncState(mediaPlayer);
		const actionPlayer = new FileActionPlayer(this.recording.document, this.recording.actions, syncState, renderController);

		const recordingPlayer = new RecordingPlayer(mediaPlayer, actionPlayer);

		const toolbarPresenter = new ToolbarPresenter(toolbarView, this, recordingPlayer);
		toolbarPresenter.setSearchService(searchService);
		toolbarPresenter.initialize();

		const playerPresenter = new PlayerPresenter(playerView, this, recordingPlayer);
		playerPresenter.initialize();

		const sidebarPresenter = new SidebarPresenter(sidebarView, this, recordingPlayer);
		sidebarPresenter.initialize();

		// Register child presenters for future disposal.
		this.addChild(toolbarPresenter);
		this.addChild(playerPresenter);
		this.addChild(sidebarPresenter);
	}

	setSidebarPosition(side: Side): void {
		this.view.setSidebarPosition(side);

		this.view.getPlayerView().getSlideView().repaint();
	}

	private executeSidebarPosition(command: Command<PlaybackPresenter>): Promise<void> {
		return command.execute(this);
	}

}

export { PlaybackPresenter };