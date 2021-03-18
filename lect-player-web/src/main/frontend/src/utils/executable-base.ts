import { Executable } from "./executable";
import { ExecutableState } from "./executable-state";

abstract class ExecutableBase extends Executable {

	init(): void {
		this.setState(ExecutableState.Initializing);

		try {
			this.initInternal();
		}
		catch (e) {
			this.setState(ExecutableState.Error);

			throw new Error(`Failed to init ${this.constructor.name}: ${e}`);
		}

		this.setState(ExecutableState.Initialized);
	}

	protected abstract initInternal(): void;
	
	start(): void {
		if (this.created() || this.destroyed()) {
			this.init();
		}

		this.setState(ExecutableState.Starting);

		try {
			this.startInternal();
		}
		catch (e) {
			this.setState(ExecutableState.Error);

			throw new Error(`Failed to start ${this.constructor.name}: ${e}`);
		}

		this.setState(ExecutableState.Started);
	}

	protected abstract startInternal(): void;
	
	stop(): void {
		this.setState(ExecutableState.Stopping);

		try {
			this.stopInternal();
		}
		catch (e) {
			this.setState(ExecutableState.Error);

			throw new Error(`Failed to stop ${this.constructor.name}: ${e}`);
		}

		this.setState(ExecutableState.Stopped);
	}

	protected abstract stopInternal(): void;

	suspend(): void {
		this.setState(ExecutableState.Suspending);

		try {
			this.suspendInternal();
		}
		catch (e) {
			this.setState(ExecutableState.Error);

			throw new Error(`Failed to suspend ${this.constructor.name}: ${e}`);
		}

		this.setState(ExecutableState.Suspended);
	}

	protected suspendInternal(): void {

	}

	destroy(): void {
		if (this.started() || this.suspended()) {
			stop();
		}

		this.setState(ExecutableState.Destroying);

		try {
			this.destroyInternal();
		}
		catch (e) {
			this.setState(ExecutableState.Error);

			throw new Error(`Failed to destroy ${this.constructor.name}: ${e}`);
		}

		this.setState(ExecutableState.Destroyed);
	}

	protected abstract destroyInternal(): void;
	
}

export { ExecutableBase };