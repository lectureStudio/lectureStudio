import { Command } from "./command";

/**
 * The FullscreenCommand requests the document to enter or to exit the
 * full-screen mode.
 */
class FullscreenCommand implements Command<void> {

	/** Indicates wheter to enter or to exit the full-screen mode. */
	private readonly fullscreen: boolean;


	/**
	 * Creates a new FullscreenCommand with optional parameter whether to
	 * enter or to exit the full-screen mode. If no parameter is specified
	 * this command will toggle the full-screen mode.
	 * 
	 * @param fullscreen True to enter full-screen mode, false to exit.
	 */
	constructor(fullscreen?: boolean) {
		this.fullscreen = fullscreen;
	}

	execute(): Promise<void> {
		return new Promise((resolve, reject) => {
			const isFullscreen = (document as any).fullscreenElement != null;

			if (isFullscreen && this.fullscreen) {
				reject("Already in full-screen mode.");
				return;
			}

			// Test for toggle.
			const fullscreen = this.fullscreen == null ? !isFullscreen : this.fullscreen;

			if (fullscreen) {
				document.documentElement.requestFullscreen()
					.catch((error: any) => {
						reject(error);
						return;
					});
			}
			else {
				document.exitFullscreen();
			}

			resolve();
		});
	}
}

export { FullscreenCommand };