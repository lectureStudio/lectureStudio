import { View } from "./view";
import { Classroom } from "../../model/classroom";

interface StartView extends View {

	setClassrooms(classrooms: Classroom[]): void;

	setOnOpenClassroom(listener: (classroom: Classroom) => void): void;

	setOnOpenRecording(observer: (file: File) => void): void;

	setOnOpenStream(observer: () => void): void;

}

export { StartView };