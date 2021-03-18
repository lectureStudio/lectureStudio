import { PlaybackModel } from "../model/playback-model";

class MediaService {

	private readonly mediaRoot: HTMLVideoElement;

	private readonly pageModel: any[];

	private readonly playbackModel: PlaybackModel;

	private changing: boolean;


	constructor(mediaRoot: HTMLVideoElement, pageModel: any[], playbackModel: PlaybackModel) {
		this.mediaRoot = mediaRoot;
		this.pageModel = pageModel;
		this.playbackModel = playbackModel;
		this.changing = false;

		this.initialize();
	}

	seekToTime(time: number) {
		if (this.changing) {
			return;
		}
		if (time < 0 || time > this.mediaRoot.duration) {
			return;
		}

		this.mediaRoot.currentTime = time;
	}

	selectPage(index: number) {
		if (this.changing) {
			return;
		}
		if (index < 0 || index >= this.pageModel.length) {
			return;
		}

		const time = this.pageModel[index].time / 1000;

		this.seekToTime(time);
	}

	getPageIndex(time: number) {
		// Time in ms.
		time = time * 1000;

		let pageIndex = 0;

		for (let i = 0; i < this.pageModel.length; i++) {
			const pageTime = this.pageModel[i].time;

			if (pageTime <= time) {
				pageIndex = i;
			}
		}

		return pageIndex;
	}

	resize() {
		const videoRatio = this.mediaRoot.height / this.mediaRoot.width;
		const windowRatio = document.body.clientHeight / document.body.clientWidth;

		if (windowRatio < videoRatio) {
			if (document.body.clientHeight > 50) { /* smallest video height */
				this.mediaRoot.height = document.body.clientHeight * 0.8;
			}
			else {
				this.mediaRoot.height = 50;
			}
		}
		else {
			this.mediaRoot.width = document.body.clientWidth;
		}
	}

	initialize() {
		const updatePlaybackModel = () => {
			this.changing = true;
			this.playbackModel.selectedPageIndex = this.getPageIndex(this.mediaRoot.currentTime);
			this.changing = false;
		}

		this.mediaRoot.addEventListener("loadedmetadata", updatePlaybackModel);
		this.mediaRoot.addEventListener("seeking", updatePlaybackModel);
		this.mediaRoot.addEventListener("timeupdate", updatePlaybackModel);

		window.addEventListener('resize', this.resize.bind(this), false);

		// Get an initial width to work with.
		this.mediaRoot.height = 100;

		this.resize();
	}
}

export { MediaService };