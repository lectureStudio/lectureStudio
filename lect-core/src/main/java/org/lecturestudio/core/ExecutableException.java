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

/**
 * This exception is thrown to indicate a problem while operating an executable
 * component which implements the {@link Executable} interface. Throwing this
 * exception should be considered fatal to the operation of the application
 * containing this component.
 *
 * @author Alex Andres
 */
public class ExecutableException extends Exception {

	private static final long serialVersionUID = 5074925163804690325L;


	/**
	 * Construct a new ExecutableException with no other information.
	 */
	public ExecutableException() {
		super();
	}

	/**
	 * Construct a new ExecutableException with the specified message.
	 *
	 * @param message A Message describing this exception.
	 */
	public ExecutableException(String message) {
		super(message);
	}

	/**
	 * Construct a new ExecutableException with the specified formatted
	 * message.
	 *
	 * @param message A Message describing this exception.
	 * @param args    Arguments used in the formatted message.
	 */
	public ExecutableException(String message, Object... args) {
		super(String.format(message, args));
	}

	/**
	 * Construct a new ExecutableException with the specified throwable.
	 *
	 * @param throwable A Throwable that caused this exception.
	 */
	public ExecutableException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * Construct a new ExecutableException with the specified message and
	 * throwable.
	 *
	 * @param message   A Message describing this exception.
	 * @param throwable A Throwable that caused this exception.
	 */
	public ExecutableException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Construct a new ExecutableException with the specified formatted message
	 * and provided throwable.
	 *
	 * @param message   A Message describing this exception.
	 * @param throwable A Throwable that caused this exception.
	 * @param args      Arguments used in the formatted message.
	 */
	public ExecutableException(String message, Throwable throwable, Object... args) {
		super(String.format(message, args), throwable);
	}

}
