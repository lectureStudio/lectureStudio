import { View } from "./view";
import { Side } from "../../utils/side";
import { ToolbarView } from "./toolbar.view";
import { PlayerView } from "./player.view";
import { SidebarView } from "./sidebar.view";

interface PlaybackView extends View {

	getSidebarView(): SidebarView;
	
	getToolbarView(): ToolbarView;

	getPlayerView(): PlayerView;

	setSidebarPosition(side: Side): void;

}

export { PlaybackView };