export interface Listener<T> {

	(event: T): void;

}

export interface Disposable {

	dispose(): void;

}

export class TypedEvent<T> {

	private listeners: Listener<T>[] = [];
	private listenersOncer: Listener<T>[] = [];


	subscribe(listener: Listener<T>): Disposable {
		this.listeners.push(listener);

		return {
			dispose: () => this.unsubscribe(listener)
		};
	}

	subscribeOnce(listener: Listener<T>): void {
		this.listenersOncer.push(listener);
	}

	unsubscribe(listener: Listener<T>): void {
		const callbackIndex = this.listeners.indexOf(listener);

		if (callbackIndex > -1) {
			this.listeners.splice(callbackIndex, 1);
		}
	}

	publish(event: T): void {
		this.listeners.forEach((listener) => listener(event));

		this.listenersOncer.forEach((listener) => listener(event));
		this.listenersOncer = [];
	}

	pipe(te: TypedEvent<T>): Disposable {
		return this.subscribe((e) => te.publish(e));
	}
}

//export { Listener, Disposable, TypedEvent };