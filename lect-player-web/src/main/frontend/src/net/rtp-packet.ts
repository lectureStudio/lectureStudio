/**
 * This class represents a RTP packet. The RtpPacket holds all RTP header fields that
 * can be set or accessed.
 * 
 * @author Alex Andres
 * 
 */
class RtpPacket {

	/**
	 * RTP data header
	 */

	/** Protocol version */
	version: number; // 2 bits

	/** Padding flag */
	padding: number; // 1 bit

	/** Header extension flag */
	extension: number; // 1 bit

	/** Marker bit */
	marker: number; // 1 bit

	/** Payload type */
	payloadType: number; // 7 bits

	/** Sequence number */
	seqNumber: number; // 16 bits

	/** Timestamp */
	timestamp: number; // 32 bits

	/** Synchronization source */
	ssrc: number; // 32 bits

	/** Optional CSRC list */
	csrc: number[]; // 32 x n bits, n < 16

	/**
	 * RTP payload data
	 */

	/** Actual data, without header */
	payload: Uint8Array;


	constructor() {
		this.version = 2;
		this.extension = 0;
		this.marker = 0;
	}

	static deserialize(data: Uint8Array): RtpPacket {
		if (data.byteLength < 12) {
			throw new Error("Insufficient data to deserialize RtpPacket");
		}

		const packet = new RtpPacket();

		/* Parse RTP header */
		packet.version = (data[0] & 0xc0) >>> 6;
		packet.padding = (data[0] & 0x20) >>> 5;
		packet.extension = (data[0] & 0x10) >>> 4;
		const CC = (data[0] & 0x0f);

		packet.marker = (data[1] & 0x80) >> 7;
		packet.payloadType = (data[1] & 0x7F);
		packet.seqNumber = ((data[2] & 0xff) << 8) | (data[3] & 0xff);

		packet.timestamp = ((data[4] & 0xff) << 24) | ((data[5] & 0xff) << 16) | ((data[6] & 0xff) << 8) | (data[7] & 0xff);
		packet.timestamp = packet.timestamp >>> 0; // Convert to UINT32 with the Unsigned Right Shift Operator ( >>> )

		packet.ssrc = ((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16) | ((data[10] & 0xff) << 8) | (data[11] & 0xff);
		packet.ssrc = packet.ssrc >>> 0;

		if (CC > 0) {
			packet.csrc = new Array<number>(CC);

			for (let i = 0; i < CC; i++) {
				const offset = 12 + i * 4;
				packet.csrc[i] = (data[offset] << 24) + (data[offset + 1] << 16) | (data[offset + 2] << 8) | data[offset + 3];
			}
		}

		const offset = 12 + 4 * CC;

		packet.payload = data.slice(offset);

		return packet;
	}

	public toString(): string {
		const csrcLength = this.csrc == null ? 0 : this.csrc.length;
		let csrc = "";

		for (let i = 0; i < csrcLength; i++) {
			csrc += "CSRC: " + this.csrc[i];
		}

		const str = new Array<string>();
		str.push("[RTP Packet]");
		str.push(`V: ${this.version} P: ${this.padding} X: ${this.extension} CC: ${csrcLength}`);
		str.push(`M: ${this.marker} PT: ${this.payloadType} Seq: ${this.seqNumber}`);
		str.push(`Timestamp: ${this.timestamp}`);
		str.push(`SSRC: ${this.ssrc}`);

		if (csrc) {
			str.push(`${csrc}`);
		}

		str.push(`Data length: ${this.payload.length}`);

		return str.join(" ");
	}
}

export { RtpPacket };