import { ExecutableState } from "./executable-state";
import { TypedEvent, Listener, Disposable } from "./event-listener";

abstract class Executable {

	protected readonly stateEvent = new TypedEvent<ExecutableState>();

	protected _state: ExecutableState = ExecutableState.Created;

	protected _prevState: ExecutableState = ExecutableState.Created;


	abstract init(): void;

	abstract start(): void;

	abstract stop(): void;

	abstract suspend(): void;

	abstract destroy(): void;


	get state(): ExecutableState {
		return this._state
	}

	get previousState(): ExecutableState {
		return this._prevState;
	}

	initialized(): boolean {
		return this._state === ExecutableState.Initialized;
	}

	created(): boolean {
		return this._state === ExecutableState.Created;
	}

	started(): boolean {
		return this._state === ExecutableState.Started;
	}

	stopped(): boolean {
		return this._state === ExecutableState.Stopped;
	}

	suspended(): boolean {
		return this._state === ExecutableState.Suspended;
	}

	destroyed(): boolean {
		return this._state === ExecutableState.Destroyed;
	}

	error(): boolean {
		return this._state === ExecutableState.Error;
	}

	addStateListener(listener: Listener<ExecutableState>): Disposable {
		return this.stateEvent.subscribe(listener);
	}

	removeStateListener(listener: Listener<ExecutableState>): void {
		this.stateEvent.unsubscribe(listener);
	}

	protected fireStateChanged(): void {
		this.stateEvent.publish(this._state);
	}

	protected setState(state: ExecutableState): void {
		//console.log(`Setting state for ${this.constructor.name} to ${state}`);

		if (!this.validateNextState(state)) {
			throw new Error(`Invalid state transition for ${this.constructor.name}: [${this.state}] -> [${state}].`);
		}

		this._prevState = this._state;
		this._state = state;

		this.fireStateChanged();
	}

	private validateNextState(nextState: ExecutableState): boolean {
		switch (this._state) {
			case ExecutableState.Created:
				return this.isAllowed(nextState,
					ExecutableState.Initializing,
					ExecutableState.Destroying);

			case ExecutableState.Initializing:
				return this.isAllowed(nextState,
					ExecutableState.Initialized,
					ExecutableState.Error);

			case ExecutableState.Initialized:
				return this.isAllowed(nextState,
					ExecutableState.Starting,
					ExecutableState.Destroying);

			case ExecutableState.Starting:
				return this.isAllowed(nextState,
					ExecutableState.Started,
					ExecutableState.Error);

			case ExecutableState.Started:
				return this.isAllowed(nextState,
					ExecutableState.Suspending,
					ExecutableState.Stopping,
					ExecutableState.Destroying,
					ExecutableState.Error);

			case ExecutableState.Stopping:
				return this.isAllowed(nextState,
					ExecutableState.Stopped,
					ExecutableState.Error);

			case ExecutableState.Stopped:
				return this.isAllowed(nextState,
					ExecutableState.Starting,
					ExecutableState.Destroying);

			case ExecutableState.Suspending:
				return this.isAllowed(nextState,
					ExecutableState.Suspended,
					ExecutableState.Error);

			case ExecutableState.Suspended:
				return this.isAllowed(nextState,
					ExecutableState.Starting,
					ExecutableState.Stopping,
					ExecutableState.Destroying);

			case ExecutableState.Destroying:
				return this.isAllowed(nextState,
					ExecutableState.Destroyed,
					ExecutableState.Error);

			case ExecutableState.Destroyed:
				return this.isAllowed(nextState,
					ExecutableState.Initializing);

			case ExecutableState.Error:
				// Allow to recover from previous operation failure.
				return this.isAllowed(nextState,
					ExecutableState.Starting,
					ExecutableState.Stopping,
					ExecutableState.Destroying);

			default:
				return false;
		}
	}

	private isAllowed(nextState: ExecutableState, ...allowedStates: ExecutableState[]): boolean {
		if (!allowedStates) {
			throw new Error("No allowed states provided.");
		}

		for (let allowedState of allowedStates) {
			if (nextState == allowedState) {
				return true;
			}
		}

		return false;
	}
}

export { Executable };