import { Command } from "./command";
import { Side } from "../utils/side";
import { PlaybackPresenter } from "../api/presenter/playback.presenter";

class SidebarPositionCommand implements Command<PlaybackPresenter> {

	private readonly side: Side;


	constructor(side: Side) {
		this.side = side;
	}

	execute(presenter: PlaybackPresenter): Promise<void> {
		return new Promise((resolve, reject) => {
			presenter.setSidebarPosition(this.side);

			resolve();
		});
	}
}

export { SidebarPositionCommand };