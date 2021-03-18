import { Observable } from "../utils/observable";

class Property<T> extends Observable<T> {

	private _value: T;


	constructor(defaultValue?: T) {
		super();

		this._value = defaultValue;
	}

	get value(): T {
		return this._value;
	}

	set value(newValue: T) {
		if (this._value === newValue) {
			return;
		}

		this._value = newValue;

		this.notify(this._value);
	}

	notifyObservers(): void {
		super.notify(this._value);
	}
}

export { Property };