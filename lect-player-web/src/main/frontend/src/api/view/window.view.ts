import { View } from "./view";

interface WindowView extends View {

	setView(view: View): void;

}

export { WindowView };