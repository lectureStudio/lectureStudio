import { Locale } from "./locale";
import { ClassroomDocument } from "./classroom-document";
import { ClassroomService } from "./classroom-service";

export interface Classroom {

	/** The unique classroom identifier. */
	readonly id: number;

	/** The name of the classroom. */
	readonly name: string;

	/** The short name of the classroom which is used mainly for URLs. */
	readonly shortName: string;

	/** The language of the classroom. */
	readonly locale: Locale;

	/** The timestamp of when the classroom was created. */
	readonly createdTimestamp: number;

	/** The initial list of documents used in this classroom. */
	readonly documents: ClassroomDocument[];

	/** The active services of the classroom. */
	readonly services: ClassroomService[];

}