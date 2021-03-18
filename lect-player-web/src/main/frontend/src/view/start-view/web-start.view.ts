import { ViewElement } from "../view-element";
import { Classroom } from "../../model/classroom";
import { WebViewElement } from "../web-view-element";
import { StartView } from "../../api/view/start.view";

@ViewElement({
	selector: "start-view",
	templateUrl: "web-start.view.html",
	styleUrls: ["web-start.view.css"]
})
class WebStartView extends WebViewElement implements StartView {

	private onOpenClassroom: (classroom: Classroom) => void;

	private openRecordingButton: HTMLElement;

	private openStreamButton: HTMLElement;

	private classrooms: Classroom[];


	connectedCallback() {
		this.render();
	}

	setClassrooms(classrooms: Classroom[]): void {
		this.classrooms = classrooms;

		this.render();

		const items = this.querySelectorAll(".classroom-item");
		items.forEach((item: Node, index: number) => {
			item.addEventListener("click", () => {
				if (this.onOpenClassroom) {
					this.onOpenClassroom(this.classrooms[index]);
				}
			});
		});
	}

	setOnOpenClassroom(listener: (classroom: Classroom) => void): void {
		this.onOpenClassroom = listener;
	}

	setOnOpenRecording(observer: (file: File) => void): void {
		this.openRecordingButton.addEventListener("change", (event: Event) => {
			var input = <HTMLInputElement>event.target;
			const file = input.files[0];

			observer(file);
		}, false);
	}

	setOnOpenStream(observer: () => void): void {
		//this.openStreamButton.addEventListener("click", observer);
	}

	private formatClassroomDate() {
		return (createdTimestamp: string, render: Function) => {
			const time = parseInt(render(createdTimestamp));
			const date = new Date(time);

			return date.getHours() + ":" + date.getMinutes();
		};
	}
}

export { WebStartView };