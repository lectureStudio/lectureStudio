/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sub-classes may extend this executable base class by only implementing the
 * internal life cycle methods. This base implementation of the {@link
 * Executable} interface handles the proper state transition rules for the life
 * cycle methods.
 *
 * @author Alex Andres
 */
public abstract class ExecutableBase implements Executable {

	private static final Logger LOG = LogManager.getLogger(ExecutableBase.class);

	/** The list of registered state listeners for event notifications. */
	private final List<ExecutableStateListener> stateListeners = new CopyOnWriteArrayList<>();

	/** The current state of this component. */
    private volatile ExecutableState state = ExecutableState.Created;
    
    /** The previous state of this component. */
    private volatile ExecutableState prevState = ExecutableState.Created;


	/**
	 * Adds an {@code ExecutableStateListener} to this component.
	 *
	 * @param listener The listener to add.
	 */
	public void addStateListener(ExecutableStateListener listener) {
		stateListeners.add(listener);
	}

	/**
	 * Removes an {@code ExecutableStateListener} from this component.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeStateListener(ExecutableStateListener listener) {
		stateListeners.remove(listener);
	}

	@Override
	public final synchronized void init() throws ExecutableException {
		setState(ExecutableState.Initializing);
		
		try {
			initInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to initialize Executable component [%s]", e, this);
		}
		
		setState(ExecutableState.Initialized);
	}

	/**
	 * @throws ExecutableException if the sub-class fails to initialize this
	 *                             component.
	 */
	protected abstract void initInternal() throws ExecutableException;
	
	@Override
    public final synchronized void start() throws ExecutableException {
    	if (created() || destroyed()) {
            init();
        }
    	
    	setState(ExecutableState.Starting);
    	
    	try {
			startInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to start Executable component [%s]", e, this);
		}
    	
    	setState(ExecutableState.Started);
    }

	/**
	 * @throws ExecutableException if the sub-class fails to start this
	 *                             component.
	 */
	protected abstract void startInternal() throws ExecutableException;
    
	@Override
	public final synchronized void stop() throws ExecutableException {
		setState(ExecutableState.Stopping);

		try {
			stopInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to stop Executable component [%s]", e, this);
		}

		setState(ExecutableState.Stopped);
	}

	/**
	 * @throws ExecutableException if the sub-class fails to stop this component.
	 */
	protected abstract void stopInternal() throws ExecutableException;
    
	@Override
	public final synchronized void suspend() throws ExecutableException {
		setState(ExecutableState.Suspending);

		try {
			suspendInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to suspend Executable component [%s]", e, this);
		}

		setState(ExecutableState.Suspended);
	}
	
	/**
	 * This method is meant to be overridden by sub-classes in order to implement
	 * a custom suspend routine, if required.
	 * 
	 * @throws ExecutableException if the sub-class fails to stop this component.
	 */
	protected void suspendInternal() throws ExecutableException {
		
	}
	
	@Override
    public final synchronized void destroy() throws ExecutableException {
    	if (started() || suspended()) {
            stop();
        }
    	
    	setState(ExecutableState.Destroying);

		try {
			destroyInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to destroy Executable component [%s]", e, this);
		}

		setState(ExecutableState.Destroyed);
    }

	/**
	 * @throws ExecutableException if the subclass fails to destroy this
	 *                             component.
	 */
	protected abstract void destroyInternal() throws ExecutableException;
    
	@Override
	public ExecutableState getState() {
		return state;
	}
	
	/**
	 * Obtain the previous state of this component.
	 * 
	 * @return The previous state of this component.
	 */
	public ExecutableState getPreviousState() {
		return prevState;
	}

	/**
	 * Update the component state if, and only if, the attempted state transition
	 * is valid.
	 *
	 * @param state The new state for this component.
	 * 
	 * @exception ExecutableException if the state transition fails.
	 */
	protected final synchronized void setState(ExecutableState state) throws ExecutableException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Setting state for [{}] to [{}]", this, state);
        }
		
		if (!validateNextState(state)) {
			throw new ExecutableException("Invalid state transition for Executable component [%s] in state [%s] to [%s]",
					this, getState(), state);
		}
		
		this.prevState = this.state;
		this.state = state;
		
		fireStateChanged();
	}

	/**
	 * Notify state listeners about the new state. This method can be overridden
	 * by subclasses in order to use a custom state notification.
	 */
	protected void fireStateChanged() {
		for (ExecutableStateListener listener : stateListeners) {
			listener.onExecutableStateChange(prevState, state);
		}
	}

	final protected void logDebugMessage(String message, Object... messageParams) {
		LOG.debug(MessageFormat.format(message, messageParams));
	}

	final protected void logTraceMessage(String message, Object... messageParams) {
		LOG.trace(MessageFormat.format(message, messageParams));
	}

	final protected void logErrorMessage(String message, Object... messageParams) {
		LOG.error(MessageFormat.format(message, messageParams));
	}

	final protected void logException(Throwable throwable, String throwMessage) {
		requireNonNull(throwable);
		requireNonNull(throwMessage);

		LOG.error(throwMessage, throwable);
	}

	private boolean validateNextState(ExecutableState nextState) {
		switch (this.state) {
			case Created:
				return isAllowed(nextState,
						ExecutableState.Initializing,
						ExecutableState.Destroying);

			case Initializing:
				return isAllowed(nextState,
						ExecutableState.Initialized,
						ExecutableState.Error);

			case Initialized:
				return isAllowed(nextState,
						ExecutableState.Starting,
						ExecutableState.Destroying);

			case Starting:
				return isAllowed(nextState,
						ExecutableState.Started,
						ExecutableState.Error);

			case Started:
				return isAllowed(nextState,
						ExecutableState.Suspending,
						ExecutableState.Stopping,
						ExecutableState.Destroying,
						ExecutableState.Error);

			case Stopping:
				return isAllowed(nextState,
						ExecutableState.Stopped,
						ExecutableState.Error);

			case Stopped:
				return isAllowed(nextState,
						ExecutableState.Starting,
						ExecutableState.Destroying);

			case Suspending:
				return isAllowed(nextState,
						ExecutableState.Suspended,
						ExecutableState.Error);

			case Suspended:
				return isAllowed(nextState,
						ExecutableState.Starting,
						ExecutableState.Stopping,
						ExecutableState.Destroying);

			case Destroying:
				return isAllowed(nextState,
						ExecutableState.Destroyed,
						ExecutableState.Error);

			case Destroyed:
				return isAllowed(nextState,
						ExecutableState.Initializing);

			case Error:
				// Allow to recover from previous operation failure.
				return isAllowed(nextState,
						ExecutableState.Starting,
						ExecutableState.Stopping,
						ExecutableState.Destroying);

			default:
				return false;
		}
	}

	private boolean isAllowed(ExecutableState nextState, ExecutableState... allowedStates) {
		if (isNull(allowedStates)) {
			throw new NullPointerException("No allowed states provided.");
		}

		for (ExecutableState allowedState : allowedStates) {
			if (nextState == allowedState) {
				return true;
			}
		}

		return false;
	}

}
