import { RtpPacketDecoder } from "./rtp-packet.decoder";
import { RtpPacket } from "../rtp-packet";

class RtpAudioDecoder implements RtpPacketDecoder<Uint8Array> {

	decode(packet: RtpPacket): Uint8Array {
		return packet.payload;
	}

}

export { RtpAudioDecoder };