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

package org.lecturestudio.core.app;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;

/**
 * Base Application implementation that manages the application life cycle
 * methods. Sub-classes may extend this base class by only implementing the
 * internal life cycle methods. This base implementation of the {@link
 * Application} interface handles the proper state transition rules.
 *
 * @author Alex Andres
 */
public abstract class ApplicationBase implements Application {

	static {
		try (InputStream stream = ApplicationBase.class.getResourceAsStream("/log.properties")) {
			if (nonNull(stream)) {
				java.util.logging.LogManager.getLogManager().readConfiguration(stream);
			}
		}
		catch (IOException e) {
			// Ignore
		}
	}

	/** Logger for {@link ApplicationBase} */
	private static final Logger LOG = LogManager.getLogger(ApplicationBase.class);

	/** ArrayList containing all open files. */
	protected static final List<File> OPEN_FILES = new ArrayList<>();

	/**
	 * The handler is notified when the application is asked to open a list of
	 * files.
	 */
	protected static Consumer<List<File>> openFilesHandler;

	/** The list of all registered state listeners. */
	private final List<ApplicationStateListener> stateListeners = new ArrayList<>();

	/** The current state of the application. */
	private ExecutableState state = ExecutableState.Created;


	/**
	 * Not to be called directly.
	 * <p>
	 * Subclasses can provide a no-args constructor to initialize the private
	 * final state. Anything else that might refer to public API, should be done
	 * in the {@link #init(String[])} and {@link #start()} method.
	 */
	protected ApplicationBase() {

	}

	@Override
	public final synchronized void init(final String[] args) throws ExecutableException {
		setState(ExecutableState.Initializing);

		if (args.length > 0) {
			// First argument must be the file to open.
			String fileEncoding = System.getProperty("file.encoding");
			String utf8Path;

			try {
				utf8Path = new String(args[0].getBytes(fileEncoding),
						StandardCharsets.UTF_8);
			}
			catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			File file = new File(utf8Path);

			OPEN_FILES.add(file);
		}

		try {
			initInternal(args);
		}
		catch (Exception e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to initialize Executable component [%s].", e, this);
		}

		setState(ExecutableState.Initialized);
	}

	@Override
	public final synchronized void start() throws ExecutableException {
		setState(ExecutableState.Starting);

		try {
			startInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to start Executable component [%s].", e, this);
		}

		setState(ExecutableState.Started);
	}

	@Override
	public final synchronized void stop() throws ExecutableException {
		setState(ExecutableState.Stopping);

		try {
			stopInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to stop Executable component [%s].", e, this);
		}

		setState(ExecutableState.Stopped);
	}

	@Override
	public final synchronized void destroy() throws ExecutableException {
		if (state == ExecutableState.Started) {
			stop();
		}

		setState(ExecutableState.Destroying);

		try {
			destroyInternal();
		}
		catch (ExecutableException e) {
			setState(ExecutableState.Error);

			throw new ExecutableException("Failed to destroy Executable component [%s].", e, this);
		}

		setState(ExecutableState.Destroyed);

		System.exit(0);
	}

	@Override
	public final void addStateListener(ApplicationStateListener listener) {
		requireNonNull(listener, "ApplicationStateListener must not be null.");

		if (!stateListeners.contains(listener)) {
			stateListeners.add(listener);
		}
	}

	@Override
	public final void removeStateListener(ApplicationStateListener listener) {
		requireNonNull(listener, "ApplicationStateListener must not be null.");

		stateListeners.remove(listener);
	}

	@Override
	public final synchronized ExecutableState getState() {
		return state;
	}

	/**
	 * Notify state listeners about the new state.
	 */
	protected final void fireStateChanged() {
		stateListeners.forEach(listener -> listener.applicationState(getState()));
	}

	/**
	 * @throws ExecutableException If the sub-class fails to initialize this
	 *                             application.
	 */
	protected abstract void initInternal(final String[] args)
			throws ExecutableException;

	/**
	 * @throws ExecutableException If the sub-class fails to start this
	 *                             application.
	 */
	protected abstract void startInternal() throws ExecutableException;

	/**
	 * @throws ExecutableException If the sub-class fails to stop this
	 *                             application.
	 */
	protected abstract void stopInternal() throws ExecutableException;

	/**
	 * @throws ExecutableException If the sub-class fails to destroy this
	 *                             application.
	 */
	protected abstract void destroyInternal() throws ExecutableException;

	/**
	 * Creates an instance of the concrete {@code Application} subclass, then
	 * calls the sequence of following methods:
	 * <li>{@link #init(String[])}
	 * <li>{@link #start()}
	 * <p>
	 * If a {@link Preloader} class was specified via the system property
	 * "application.preloader", this concrete preloader will be instantiated
	 * prior the application startup routine lasting as long as the application
	 * is initializing. Once the application reaches the starting state, the
	 * preloader will be closed.
	 *
	 * @param args The main method's arguments.
	 */
	public static void launch(final String[] args) {
		try {
			Class<? extends Preloader> preloaderClass = null;

			String preloaderByProperty = AccessController.doPrivileged((PrivilegedAction<String>) () -> {
				return System.getProperty("application.preloader");
			});

			if (nonNull(preloaderByProperty)) {
				Class<?> pClass = null;

				try {
					pClass = Class.forName(preloaderByProperty, false, Thread.currentThread().getContextClassLoader());
				}
				catch (Exception e) {
					LOG.warn("Could not load application preloader class.", e);
				}

				if (nonNull(pClass)) {
					if (Preloader.class.isAssignableFrom(pClass)) {
						preloaderClass = (Class<? extends Preloader>) pClass;
					}
					else {
						LOG.warn("Preloader class is not a subclass of " + Preloader.class.getName() + ".");
					}
				}
			}

			launch(args, preloaderClass);
		}
		catch (Exception e) {
			LOG.fatal("Could not launch application.", e);

			// Hard exit.
			System.exit(0);
		}
	}

	/**
	 * Creates an instance of the concrete {@code Application} subclass, then
	 * calls the sequence of following methods:
	 * <li>{@link #init(String[])}
	 * <li>{@link #start()}
	 * <p>
	 * The specified {@link Preloader} class will be instantiated prior the
	 * application startup routine lasting as long as the application is
	 * initializing. Once the application reaches the starting state, the
	 * preloader will be closed.
	 *
	 * @param args           The main method's arguments.
	 * @param preloaderClass The class of the preloader to show while the
	 *                       application is loading.
	 */
	public static void launch(final String[] args, Class<? extends Preloader> preloaderClass) {
		try {
			StackTraceElement[] cause = Thread.currentThread().getStackTrace();
			Class<? extends Application> appClass = null;

			for (StackTraceElement se : cause) {
				String className = se.getClassName();
				String methodName = se.getMethodName();

				Class<?> callingClass = Class.forName(className, false, Thread.currentThread().getContextClassLoader());

				if ("main".equals(methodName) && Application.class.isAssignableFrom(callingClass)) {
					appClass = (Class<? extends Application>) callingClass;
					break;
				}
			}

			requireNonNull(appClass, "No application class found.");

			launch(args, appClass, preloaderClass);
		}
		catch (Exception e) {
			LOG.fatal("Could not launch application.", e);

			// Hard exit.
			System.exit(0);
		}
	}

	private static void launch(final String[] args,
			Class<? extends Application> appClass,
			Class<? extends Preloader> preloaderClass) throws Exception {
		requireNonNull(appClass, "Application class must not be null.");

		Preloader preloader = null;

		if (nonNull(preloaderClass)) {
			try {
				preloader = preloaderClass.getConstructor().newInstance();
				preloader.init(args);
				preloader.start();
			}
			catch (Exception e) {
				LOG.warn("Start preloader failed.", e);
			}
		}

		StateListener stateListener = new StateListener(preloader);

		Application application = appClass.getConstructor().newInstance();
		application.addStateListener(stateListener);
		application.init(args);
		application.start();

		if (nonNull(stateListener.getException())) {
			throw stateListener.getException();
		}

		application.removeStateListener(stateListener);
	}

	/**
	 * Update the component state if, and only if, the attempted state
	 * transition is valid.
	 *
	 * @param state The new state for this component.
	 *
	 * @throws ExecutableException If the state transition fails.
	 */
	private synchronized void setState(ExecutableState state) throws ExecutableException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Setting state for [{}] to [{}]", this, state);
		}

		if (!validateNextState(state)) {
			throw new ExecutableException(
					"Invalid state transition for Executable component [%s] in state [%s] to [%s].",
					this, getState(), state);
		}

		this.state = state;

		fireStateChanged();
	}

	/**
	 * Checks if {@code nextState} is allowed the be the next state after the current {@code state} of the application.
	 *
	 * @param nextState The state that is desired to be the next state.
	 * @return 			true if {@code nextState} is allowed the be the next state after the current {@code state} of
	 * 					the application and false otherwise.
	 */
	private boolean validateNextState(ExecutableState nextState) {
		switch (this.state) {
			case Created:
				return isAllowed(nextState, ExecutableState.Initializing, ExecutableState.Destroying);

			case Initializing:
				return isAllowed(nextState, ExecutableState.Initialized, ExecutableState.Error);

			case Initialized:
				return isAllowed(nextState, ExecutableState.Starting, ExecutableState.Destroying);

			case Starting:
				return isAllowed(nextState, ExecutableState.Started, ExecutableState.Error);

			case Started:
				return isAllowed(nextState, ExecutableState.Stopping, ExecutableState.Destroying);

			case Stopping:
				return isAllowed(nextState, ExecutableState.Stopped, ExecutableState.Error);

			case Stopped:
				return isAllowed(nextState, ExecutableState.Starting, ExecutableState.Destroying);

			case Destroying:
				return isAllowed(nextState, ExecutableState.Destroyed, ExecutableState.Error);

			case Destroyed:
				return isAllowed(nextState, ExecutableState.Initializing);

			case Error:
				// Allow to recover from previous operation failure.
				return isAllowed(nextState, ExecutableState.Starting, ExecutableState.Stopping, ExecutableState.Destroying);

			default:
				return false;
		}
	}

	/**
	 * Checks if {@code nextState} is a allowed to be the next state.
	 *
	 * @param nextState The state that is desired to be the next state.
	 * @param allowedStates All the states that are possible to be the next state.
	 * @return {@code true} if {@code allowedStates} contains {@code nextState}, otherwise {@code false}.
	 */
	private boolean isAllowed(ExecutableState nextState, ExecutableState... allowedStates) {
		requireNonNull(allowedStates, "No allowed states provided.");

		for (ExecutableState allowedState : allowedStates) {
			if (nextState == allowedState) {
				return true;
			}
		}

		return false;
	}

	private static class StateListener implements ApplicationStateListener {

		/** The stateListeners preloader */
		private final Preloader preloader;

		/** Possible exception caught in {@link #applicationState(ExecutableState)}  */
		private Exception exception;

		/**
		 * Create a new {@link StateListener} with the specified preloader.
		 *
		 * @param preloader the currently active preloader
		 */
		StateListener(Preloader preloader) {
			this.preloader = preloader;
		}

		@Override
		public void applicationState(ExecutableState state) {
			if (state == ExecutableState.Starting) {
				if (nonNull(preloader)) {
					try {
						preloader.close();
						preloader.destroy();
					}
					catch (Exception e) {
						exception = e;
					}
				}
			}
		}

		/**
		 * Obtain the exception of this {@link StateListener}.
		 *
		 * @return The exception of this {@link StateListener}.
		 */
		public Exception getException() {
			return exception;
		}
	}
}
