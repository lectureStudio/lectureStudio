import { ToolbarView } from "../view/toolbar.view";
import { Presenter } from "./presenter";
import { OpenRecordingCommand } from "../../command/window/open-recording.command";
import { SidebarPositionCommand } from "../../command/sidebar-position.command";
import { SideUtil } from "../../utils/side";
import { RecordingPlayer } from "../../media/recording-player";
import { View } from "../view/view";
import { SearchService } from "../../service/search-service";
import { SearchResult } from "../../component/search/web-search";
import { WebSnackbar } from "../../component/snackbar/web-snackbar";

class ToolbarPresenter extends Presenter<ToolbarView> {

	private recordingPlayer: RecordingPlayer;

	private searchService: SearchService;


	constructor(view: ToolbarView, parent: Presenter<View>, recordingPlayer: RecordingPlayer) {
		super(view, parent);

		this.recordingPlayer = recordingPlayer;
	}

	initialize(): void {
		this.view.setOnOpenRecording(this.onOpenRecording.bind(this));
		this.view.setOnSiderbarPosition(this.onSidebarPosition.bind(this));
		this.view.setOnSearch(this.onSearch.bind(this));
		this.view.setOnSearchNext(this.onSearchNext.bind(this));
		this.view.setOnSearchPrevious(this.onSearchPrevious.bind(this));
	}

	setSearchService(searchService: SearchService) {
		this.searchService = searchService;
	}

	private onOpenRecording(file: File) {
		if (file) {
			this.execute(new OpenRecordingCommand(file))
				.catch(error => {
					const snackbar = new WebSnackbar();
					snackbar.setText(error);
					snackbar.show();

					console.error(error);
				});
		}
	}

	private onSidebarPosition(pos: string) {
		this.execute(new SidebarPositionCommand(SideUtil.valueOf(pos)))
			.catch(error => {
				const snackbar = new WebSnackbar();
				snackbar.setText(error);
				snackbar.show();

				console.error(error);
			});
	}

	private async onSearch(query: string): Promise<SearchResult> {
		if (query === null || query.match(/^ *$/) !== null) {
			// Query string is empty or is a whitespace.
			return null;
		}

		this.searchService.search(query);

		const pageIndices = new Array();

		// Search page text.
		const doc = this.recordingPlayer.getDocument();

		for (let i = 0; i < doc.getPageCount(); i++) {
			const text = await doc.getPageText(i);

			if (text.search(new RegExp(query, "i")) !== -1) {
				pageIndices.push(i);
			}
		}

		return new SearchResult(pageIndices);
	}

	private onSearchNext(result: SearchResult): void {
		this.recordingPlayer.setSelectedPage(result.getCurrentPageIndex());
	}

	private onSearchPrevious(result: SearchResult): void {
		this.recordingPlayer.setSelectedPage(result.getCurrentPageIndex());
	}
}

export { ToolbarPresenter };