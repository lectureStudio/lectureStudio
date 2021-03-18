import { ExecutableBase } from "../utils/executable-base";

class StreamAudioPlayer extends ExecutableBase {

	private audioContext: AudioContext;

	private audioSource: AudioBufferSourceNode;

	private audioGain: GainNode;

	private mediaSource: MediaSource;

	private media: HTMLMediaElement;

	private readonly buffer: Uint8Array[];


	constructor(media: HTMLMediaElement) {
		super();

		this.media = media;
		this.media.volume = 1;

		this.buffer = [];
	}

	addAudioData(data: Uint8Array): void {
		if (!this.started()) {
			return;
		}

		this.audioContext.decodeAudioData(data.buffer)
			.then((decodedData: AudioBuffer) => {
				//this.audioSource.buffer = decodedData;
			})
			.catch(error => {
				console.error(error);

				this.stop();
			});
	}

	setVolume(volume: number): void {
		this.audioGain.gain.value = volume;
	}

	protected initInternal(): void {
		this.audioContext = new AudioContext();
		this.audioContext.onstatechange = () => {
			console.log(this.audioContext.state);
		};

		this.audioGain = this.audioContext.createGain();
		this.audioGain.connect(this.audioContext.destination);

		this.audioSource = this.audioContext.createBufferSource();
		this.audioSource.connect(this.audioGain);
	}

	protected startInternal(): void {
		this.audioSource.start();
	}

	protected suspendInternal(): void {
		this.audioSource.stop();
	}

	protected stopInternal(): void {
		this.audioSource.stop();
		this.audioSource.disconnect();

		this.audioGain.disconnect();
	}

	protected destroyInternal(): void {

	}
}

export { StreamAudioPlayer };