import { ViewElement } from "../view-element";
import { PlaybackView } from "../../api/view/playback.view";
import { Side } from "../../utils/side";
import { WebSidebarView } from "../sidebar-view/web-sidebar.view";
import { WebToolbarView } from "../toolbar-view/web-toolbar.view";
import { WebPlayerView } from "../player-view/web-player.view";
import { WebViewElement } from "../web-view-element";

@ViewElement({
	selector: "web-playback-view",
	templateUrl: "web-playback.view.html",
	styleUrls: ["web-playback.view.css"]
})
class WebPlaybackView extends WebViewElement implements PlaybackView {

	private sidebarLeft: HTMLElement;

	private sidebarRight: HTMLElement;

	private toolbar: WebToolbarView;

	private player: WebPlayerView;

	private sidebar: WebSidebarView;


	constructor() {
		super();
	}

	getToolbarView(): WebToolbarView {
		return this.toolbar;
	}

	getSidebarView(): WebSidebarView {
		return this.sidebar;
	}

	getPlayerView(): WebPlayerView {
		return this.player;
	}

	setSidebarPosition(side: Side): void {
		switch (side) {
			case Side.LEFT:
				this.sidebarLeft.appendChild(this.sidebar);
				break;

			case Side.RIGHT:
				this.sidebarRight.appendChild(this.sidebar);
				break;

			case Side.NONE:
				if (this.sidebarLeft.contains(this.sidebar)) {
					this.sidebarLeft.removeChild(this.sidebar);
				}
				if (this.sidebarRight.contains(this.sidebar)) {
					this.sidebarRight.removeChild(this.sidebar);
				}
				break;
		}
	}
}

export { WebPlaybackView };