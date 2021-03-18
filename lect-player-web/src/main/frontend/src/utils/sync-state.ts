import { MediaPlayer } from "../media/media-player";

class SyncState {

	private mediaPlayer: MediaPlayer;


	constructor(mediaPlayer: MediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}

	get audioTime(): number {
		return this.mediaPlayer.time * 1000;
	}

}

export { SyncState };