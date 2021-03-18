import { Presenter } from "./presenter";
import { SidebarView } from "../view/sidebar.view";
import { RecordingPlayer } from "../../media/recording-player";
import { View } from "../view/view";

class SidebarPresenter extends Presenter<SidebarView> {
	
	private readonly recordingPlayer: RecordingPlayer;

	private selectedPageIndex: number;


	constructor(view: SidebarView, parent: Presenter<View>, recordingPlayer: RecordingPlayer) {
		super(view, parent);

		this.recordingPlayer = recordingPlayer;
	}

	setSelectedPage(index: number): void {
		if (this.selectedPageIndex === index) {
			return;
		}

		this.view.setSelectedPageIndex(this.selectedPageIndex, index);

		this.selectedPageIndex = index;
	}

	initialize(): void {
		this.view.setDocument(this.recordingPlayer.getDocument());
		this.view.setOnPageSelect(this.onPageSelect.bind(this));

		this.recordingPlayer.getSelectedPageProperty().subscribe(this.setSelectedPage.bind(this));

		this.setSelectedPage(0);
	}

	private onPageSelect(index: number): void {
		this.recordingPlayer.setSelectedPage(index);
	}
}

export { SidebarPresenter };