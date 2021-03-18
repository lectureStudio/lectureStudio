import { Command } from "../command";
import { WindowPresenter } from "../../api/presenter/window.presenter";

interface WindowCommand extends Command<WindowPresenter> {
	
}

export { WindowCommand };