import { MediaReceiver } from "./media.receiver";
import { RtpPacket } from "../rtp-packet";
import { RtpActionDecoder } from "../decoder/rtp-action.decoder";
import { StreamDescription } from "../../model/stream-description";
import { StreamActionPlayer } from "../../action/stream-action-player";

class ActionReceiver extends MediaReceiver {

	private readonly actionPlayer: StreamActionPlayer;

	private readonly decoder: RtpActionDecoder;


	constructor(streamDesc: StreamDescription, actionPlayer: StreamActionPlayer) {
		super(streamDesc);

		this.actionPlayer = actionPlayer;
		this.decoder = new RtpActionDecoder();
	}

	protected process(packet: RtpPacket): void {
		const action = this.decoder.decode(packet);

		this.actionPlayer.addAction(action);

		console.log(action.constructor.name);
	}

}

export { ActionReceiver };