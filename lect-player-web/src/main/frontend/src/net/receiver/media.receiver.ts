import { StreamDescription } from "../../model/stream-description";
import { Executable } from "../../utils/executable";
import { ExecutableState } from "../../utils/executable-state";
import { RtpPacket } from "../rtp-packet";
import { StreamTransport } from "../../model/stream-transport";

abstract class MediaReceiver extends Executable {

	private readonly url: string;

	private socket: WebSocket;


	protected abstract process(data: RtpPacket): void;


	constructor(streamDesc: StreamDescription) {
		super();

		const transport = streamDesc.transport;

		if (transport !== StreamTransport.WS && transport !== StreamTransport.WS_TLS) {
			throw new Error("MediaReceiver only supports WebSocket transport");
		}

		const scheme = transport === StreamTransport.WS_TLS ? "wss://" : "ws://";

		this.url = scheme + streamDesc.address + ":" + streamDesc.port;
	}

	init(): void {
		this.setState(ExecutableState.Initializing);
		this.setState(ExecutableState.Initialized);
	}

	start(): void {
		if (this.created() || this.destroyed()) {
			this.init();
		}

		this.setState(ExecutableState.Starting);

		try {
			this.socket = new WebSocket(this.url);
			this.socket.binaryType = "arraybuffer";
		}
		catch (error) {
			this.setState(ExecutableState.Error);

			throw error;
		}

		this.socket.onopen = (event: Event) => {
			this.setState(ExecutableState.Started);

			console.log("connected");
		}
		this.socket.onclose = (event: CloseEvent) => {
			// Change state only due to normal closure.
			if (event.code === 1000 && !this.error()) {
				this.setState(ExecutableState.Stopped);
			}
		}
		this.socket.onerror = (event: Event) => {
			this.setState(ExecutableState.Error);
		}
		this.socket.onmessage = (event: MessageEvent) => {
			const data = event.data as ArrayBuffer;
			const packet = RtpPacket.deserialize(new Uint8Array(data)); 

			this.process(packet);
		}
	}

	stop(): void {
		if (this.socket.readyState !== WebSocket.OPEN) {
			return;
		}

		this.setState(ExecutableState.Stopping);

		this.socket.close();
	}

	suspend(): void {
		this.stop();
	}

	destroy(): void {
		if (this.started()) {
			stop();
		}
	}

}

export { MediaReceiver };