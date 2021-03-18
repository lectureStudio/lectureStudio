import { View } from "./view";
import { PlayerView } from "./player.view";

interface StreamPlaybackView extends View {

	getPlayerView(): PlayerView;

}

export { StreamPlaybackView };