import { StreamTransport } from "./stream-transport";
import { MediaType } from "./media-type";

export interface StreamDescription {

	/** The address of the media provider. */
	readonly address: string;

	/** The port of the media provider. */
	readonly port: number;

	/** The transport protocol of the stream. */
	readonly transport: StreamTransport;

	/** The media type of the stream. */
	readonly mediaType: MediaType;

}