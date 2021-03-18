/**
 * Transport enumerations to be used for media transmission. Each enumeration
 * has it's own short description which can be used by the {@link #toString()}
 * method.
 * 
 * @author Alex Andres
 */
export enum StreamTransport {

	/** UDP transport with RTP payload. */
	RTP_UDP = "RTP/UDP",

	/** TCP transport with RTP payload. */
	RTP_TCP = "RTP/TCP",

	/** TLS encrypted TCP transport with RTP payload. */
	RTP_TCP_TLS = "RTP/TCP-TLS",

	/** HTTP transport with RTP payload. */
	RTP_HTTP = "RTP/HTTP",

	/** TLS encrypted HTTP transport with RTP payload. */
	RTP_HTTP_TLS = "RTP/HTTP-TLS",

	/** Web-Server HTTP transport. */
	HTTP = "HTTP",

	/** Web-Server TLS encrypted HTTP transport. */
	HTTP_TLS = "HTTP-TLS",

	/** WebSocket transport. */
	WS = "WS",

	/** WebSocket TLS encrypted transport. */
	WS_TLS = "WS-TLS"

}