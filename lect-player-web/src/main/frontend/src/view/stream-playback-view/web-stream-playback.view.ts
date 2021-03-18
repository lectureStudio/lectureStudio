import { ViewElement } from "../view-element";
import { WebPlayerView } from "../player-view/web-player.view";
import { WebViewElement } from "../web-view-element";
import { StreamPlaybackView } from "../../api/view/stream-playback.view";

@ViewElement({
	selector: "web-stream-playback-view",
	templateUrl: "web-stream-playback.view.html",
	styleUrls: ["web-stream-playback.view.css"]
})
class WebStreamPlaybackView extends WebViewElement implements StreamPlaybackView {

	private player: WebPlayerView;


	constructor() {
		super();
	}

	getPlayerView(): WebPlayerView {
		return this.player;
	}

}

export { WebStreamPlaybackView };