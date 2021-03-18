import { ExecutableBase } from "../utils/executable-base";
import { Classroom } from "../model/classroom";
import { MediaType } from "../model/media-type";
import { ActionReceiver } from "./receiver/action.receiver";
import { ClassroomService } from "../model/classroom-service";
import { MediaReceiver } from "./receiver/media.receiver";
import { AudioReceiver } from "./receiver/audio.receiver";
import { StreamActionPlayer } from "../action/stream-action-player";
import { StreamAudioPlayer } from "../media/stream-audio-player";

class ClassroomMediaClient extends ExecutableBase {

	private readonly classroom: Classroom;

	private readonly mediaReceivers: MediaReceiver[];

	private readonly actionPlayer: StreamActionPlayer;

	private readonly audioPlayer: StreamAudioPlayer;


	constructor(classroom: Classroom, actionPlayer: StreamActionPlayer, audioPlayer: StreamAudioPlayer) {
		super();

		this.classroom = classroom;
		this.actionPlayer = actionPlayer;
		this.audioPlayer = audioPlayer;
		this.mediaReceivers = [];
	}

	protected initInternal(): void {
		const classroomServices = this.classroom.services;

		if (!classroomServices || classroomServices.length < 1) {
			throw new Error("Classroom has no services available");
		}

		const streamService = classroomServices.find(value => {
			return value._type === "stream";
		});

		if (!streamService) {
			throw new Error("Classroom has no stream service available");
		}

		this.createActionReceiver(streamService);
		this.createAudioReceiver(streamService);

		this.actionPlayer.init();
		this.audioPlayer.init();

		for (const receiver of this.mediaReceivers) {
			receiver.init();
		}
	}

	protected startInternal(): void {
		this.actionPlayer.start();
		this.audioPlayer.start();

		for (const receiver of this.mediaReceivers) {
			receiver.start();
		}
	}

	protected stopInternal(): void {
		for (const receiver of this.mediaReceivers) {
			receiver.stop();
		}

		this.actionPlayer.stop();
		this.audioPlayer.stop();
	}

	protected destroyInternal(): void {
		for (const receiver of this.mediaReceivers) {
			receiver.destroy();
		}

		this.actionPlayer.destroy();
		this.audioPlayer.destroy();
	}

	private createActionReceiver(streamService: ClassroomService) {
		const streamDesc = streamService.streamDescriptions.find(value => {
			return value.mediaType === MediaType.Event;
		});

		if (!streamDesc) {
			throw new Error("No action stream available");
		}

		const actionReceiver = new ActionReceiver(streamDesc, this.actionPlayer);

		this.mediaReceivers.push(actionReceiver);
	}

	private createAudioReceiver(streamService: ClassroomService) {
		const streamDesc = streamService.streamDescriptions.find(value => {
			return value.mediaType === MediaType.Audio;
		});

		if (!streamDesc) {
			throw new Error("No audio stream available");
		}

		const audioReceiver = new AudioReceiver(streamDesc, this.audioPlayer);

		this.mediaReceivers.push(audioReceiver);
	}
}

export { ClassroomMediaClient };