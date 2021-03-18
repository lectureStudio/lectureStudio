type Callback = () => void;

/**
 * KeyEventOptions control which events to observe.
 */
export interface KeyEventOptions {

	/**
	 * The type of key event to observe.
	 */
	type?: "keyup" | "keydown";

	/**
	 * True to keep receiving key events when a key is held down.
	 * False to receive only the first key event when a key was pressed.
	 */
	repeat?: boolean;

	/**
	 * Capture keyboard events from editable elements like INPUT, TEXTAREA, SELECT
	 * or elements having the property 'isContentEditable' set to 'true'.
	 */
	captureEditable?: boolean;
}

/**
 * The ShortcutEntry is used for internal purposes only. It describes an Map
 * entry for a specific key combination.
 */
interface ShortcutEntry {

	/**
	 * The callback to be executed upon matched key combinations.
	 */
	callback: Callback;

	/**
	 * The key event options to control which events to observe and upon match
	 * to execute the associated callback.
	 */
	options: KeyEventOptions;

}

/**
 * Shortcut key event manager that observes 'keydown' and 'keyup' keyboard events
 * on an event target. Many Shortcut managers can co-exist for an single event
 * target.
 */
export class Shortcut {

	/** The Shift key modifier constant. */
	private readonly SHIFT_MASK = 1 << 4;

	/** The Control key modifier constant. */
	private readonly CTRL_MASK = 1 << 5;

	/** The Alt key modifier constant. */
	private readonly ALT_MASK = 1 << 6;

	/** The Meta key modifier constant. */
	private readonly META_MASK = 1 << 7;

	/** The event listener attached to the managed event target. */
	private readonly eventListener: (event: KeyboardEvent) => void;

	/** The key combination hash to shortcut entry mapping. */
	private readonly shortcutMap: Map<string, ShortcutEntry>;

	/** The event target on which to listen for key events. */
	private readonly target: EventTarget;

	/** The default key configuration. */
	private readonly defaultOptions: KeyEventOptions = {
		type: "keydown",
		repeat: false,
		captureEditable: false
	};


	/**
	 * Creates a new Shortcut manager for the specified key event target. If no
	 * target is provided, the Document will be used as the default key event
	 * target.
	 * 
	 * @param target The event target on which to listen for key events.
	 */
	constructor(target?: EventTarget) {
		this.target = target ? target : document;
		this.shortcutMap = new Map();

		this.eventListener = this.onKeyEvent.bind(this);

		this.target.addEventListener("keydown", this.eventListener);
		this.target.addEventListener("keyup", this.eventListener);
	}

	/**
	 * Binds the provided key combination to a callback that will be executed when
	 * potential modifiers including the defined key in the combination are pressed
	 * or released.
	 * 
	 * @param keyCombination The key combination or an array of key combinations to observe.
	 * @param callback The callback to be called upon observed key combination.
	 * @param options The key event options to control which events to observe.
	 */
	bind(keyCombination: string | string[], callback: Callback, options?: KeyEventOptions): void {
		if (Array.isArray(keyCombination)) {
			for (const combination of keyCombination) {
				this.bindKeyCombination(combination, callback, options);
			}
		}
		else {
			this.bindKeyCombination(keyCombination, callback, options);
		}
	}

	/**
	 * Unbinds a previously bound callback with specified key combination. The
	 * callback will no longer receive any calls.
	 * 
	 * @param keyCombination The key combination to remove from observable key events.
	 */
	unbind(keyCombination: string): void {
		keyCombination = keyCombination.toLowerCase();

		this.shortcutMap.delete(this.getKeyCombinationHash(keyCombination));
	}

	/**
	 * Unbinds all event listeners and releases resources. After this method
	 * finishes further calls on any method of this class will have no effect.
	 */
	dispose(): void {
		this.target.removeEventListener("keydown", this.eventListener);
		this.target.removeEventListener("keyup", this.eventListener);

		this.shortcutMap.clear();
	}

	private bindKeyCombination(keyCombination: string, callback: Callback, options?: KeyEventOptions): void {
		keyCombination = keyCombination.toLowerCase();

		const hash = this.getKeyCombinationHash(keyCombination);
		let entry = this.shortcutMap.get(hash);

		if (entry) {
			console.warn(`Overriding already registered key combination "${keyCombination}"`);
		}

		options = { ...this.defaultOptions, ...options };

		entry = {
			callback: callback,
			options: options
		};

		this.shortcutMap.set(hash, entry);
	}

	private onKeyEvent(event: KeyboardEvent): void {
		if (!event.key) {
			return;
		}

		let hash = this.getKeyEventHash(event);
		let entry = this.shortcutMap.get(hash);

		if (!entry) {
			/**
			 * Try a key without modifiers. Some languages require a modifier for a
			 * specific key (e.g. shift for the '?'). This way it is possible to simply
			 * bind the '?' keystroke and getting the associated callback to be executed
			 * upon the 'shift + ?' key combination.
			 * This kind of bound keystrokes has a lower priority over the simultaneously
			 * bound key combinations with modifiers for the same key.
			 */
			hash = event.key.toLowerCase() + "0";
			entry = this.shortcutMap.get(hash);

			if (!entry) {
				return;
			}
		}
		if (!entry.options.repeat && event.repeat) {
			return;
		}
		if (entry.options.type !== event.type) {
			return;
		}

		const target: any = event.target;
		let isEditable;

		switch (target.tagName) {
			case "INPUT":
			case "TEXTAREA":
			case "SELECT":
				isEditable = true;
				break;
			default:
				isEditable = target.isContentEditable;
				break;
		}

		if (!entry.options.captureEditable && isEditable) {
			return;
		}

		entry.callback();
	}

	private extractKeys(keyCombination: string): string[] {
		// Handle special cases.
		if (keyCombination.endsWith("+")) {
			return keyCombination.split("+").filter(k => k).concat(["+"]);
		}

		return keyCombination.split("+");
	}

	private getKeyCombinationHash(keyCombination: string): string {
		const keys = this.extractKeys(keyCombination);

		let key = null;
		let modifierHash = 0;

		for (let k of keys) {
			switch (k) {
				case "alt":
					modifierHash |= this.ALT_MASK;
					break;
				case "control":
					modifierHash |= this.CTRL_MASK;
					break;
				case "shift":
					modifierHash |= this.SHIFT_MASK;
					break;
				case "meta":
					modifierHash |= this.META_MASK;
					break;
				default:
					if (key) {
						console.warn(`Multiple keys for key combination "${keyCombination}" specified`);
					}
					key = k;
					break;
			}
		}

		if (!key) {
			throw new Error(`No key (except modifiers) for key combination "${keyCombination}" specified`);
		}

		return key + modifierHash;
	}

	private getKeyEventHash(event: KeyboardEvent): string {
		let key = event.key.toLowerCase();
		let modifierHash = 0;

		// Handle special cases.
		if (key === " ") {
			key = "space";
		}

		if (event.altKey) {
			modifierHash |= this.ALT_MASK;
		}
		if (event.ctrlKey) {
			modifierHash |= this.CTRL_MASK;
		}
		if (event.shiftKey) {
			modifierHash |= this.SHIFT_MASK;
		}
		if (event.metaKey) {
			modifierHash |= this.META_MASK;
		}

		return key + modifierHash;
	}
}