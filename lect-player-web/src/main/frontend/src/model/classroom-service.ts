import { StreamDescription } from "./stream-description";

export interface ClassroomService {

	/** The unique service type description. */
	readonly _type: string;

	/** The unique service ID number of the service session. */
	readonly serviceId: string;

	readonly contextPath: string;

	/** The media stream descriptions for this classroom session. */
	readonly streamDescriptions: StreamDescription[];

}