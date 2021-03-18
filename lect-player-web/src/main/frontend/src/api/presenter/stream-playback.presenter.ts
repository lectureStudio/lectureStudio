import { Presenter } from "./presenter";
import { StreamPlaybackView } from "../view/stream-playback.view";
import { View } from "../view/view";
import { RenderController } from "../../render/render-controller";
import { FileActionExecutor } from "../../action/file-action-executor";
import { MediaPlayer } from "../../media/media-player";
import { SyncState } from "../../utils/sync-state";
import { StreamActionPlayer } from "../../action/stream-action-player";
import { StreamPlayer } from "../../media/stream-player";
import { Classroom } from "../../model/classroom";
import { SlideDocument } from "../../model/document";
import { StreamPlayerPresenter } from "./stream-player.presenter";
import { ClassroomMediaClient } from "../../net/classroom-media-client";
import { StreamAudioPlayer } from "../../media/stream-audio-player";

class StreamPlaybackPresenter extends Presenter<StreamPlaybackView> {

	private readonly classroom: Classroom;

	private readonly doc: SlideDocument;


	constructor(view: StreamPlaybackView, parent: Presenter<View>, classroom: Classroom, doc: SlideDocument) {
		super(view, parent);

		this.classroom = classroom;
		this.doc = doc;
	}

	initialize(): void {
		const playerView = this.view.getPlayerView();
		const slideView = playerView.getSlideView();

		const renderController = new RenderController();
		renderController.setActionRenderSurface(slideView.getActionRenderSurface());
		renderController.setSlideRenderSurface(slideView.getSlideRenderSurface());
		renderController.setVolatileRenderSurface(slideView.getVolatileRenderSurface());
		renderController.setTextLayerSurface(slideView.getTextLayerSurface());

		const executor = new FileActionExecutor(this.doc, renderController);

		const mediaPlayer = new MediaPlayer(playerView.getAudioElement());

		const audioPlayer = new StreamAudioPlayer(playerView.getAudioElement());

		const syncState = new SyncState(mediaPlayer);
		const actionPlayer = new StreamActionPlayer(this.doc, executor, syncState);

		const mediaClient = new ClassroomMediaClient(this.classroom, actionPlayer, audioPlayer);

		const streamPlayer = new StreamPlayer(mediaClient, mediaPlayer, actionPlayer);

		const playerPresenter = new StreamPlayerPresenter(playerView, this, streamPlayer);
		playerPresenter.initialize();

		this.addChild(playerPresenter);
	}

}

export { StreamPlaybackPresenter };