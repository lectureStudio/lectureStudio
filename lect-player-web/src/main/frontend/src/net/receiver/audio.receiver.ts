import { MediaReceiver } from "./media.receiver";
import { RtpPacket } from "../rtp-packet";
import { RtpAudioDecoder } from "../decoder/rtp-audio.decoder";
import { StreamDescription } from "../../model/stream-description";
import { StreamAudioPlayer } from "../../media/stream-audio-player";

class AudioReceiver extends MediaReceiver {

	private readonly audioPlayer: StreamAudioPlayer;

	private readonly decoder: RtpAudioDecoder;


	constructor(streamDesc: StreamDescription, audioPlayer: StreamAudioPlayer) {
		super(streamDesc);

		this.audioPlayer = audioPlayer;
		this.decoder = new RtpAudioDecoder();
	}

	protected process(packet: RtpPacket): void {
		const audioData = this.decoder.decode(packet);

		this.audioPlayer.addAudioData(audioData);
	}

}

export { AudioReceiver };