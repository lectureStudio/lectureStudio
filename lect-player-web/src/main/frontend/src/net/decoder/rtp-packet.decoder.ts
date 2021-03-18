import { RtpPacket } from "../rtp-packet";
import { Decoder } from "./decoder";

/**
 * The interface RtpPacketDecoder is implemented by a specific class that should
 * decode incoming data packets. The method {@link #decodeRtpPacket(RtpPacket)}
 * throws an {@code Error} if the packet could not be decoded.
 * 
 * @author Alex Andres
 */
interface RtpPacketDecoder<T> extends Decoder<RtpPacket, T> {

	/**
	 * Decodes incoming RTP packets to a specific object that represents the payload.
	 * 
	 * @param packet the RTP packet to decode.
	 * 
	 * @return the object decoded from the RTP packet's payload.
	 * 
	 * @throws Error if the packet could not be decoded.
	 */
	decode(packet: RtpPacket): T;

}

export { RtpPacketDecoder };