export type Observer<T> = (value: T) => void;

export class Observable<T> {

	private observers: Observer<T>[];


	constructor() {
		this.observers = [];
	}

	subscribe(observer: Observer<T>): void {
		this.observers.push(observer);
	}

	unsubscribe(observer: Observer<T>): void {
		this.observers = this.observers.filter(subscriber => subscriber !== observer);
	}

	notify(data: T): void {
		this.observers.forEach(observer => observer(data));
	}
}