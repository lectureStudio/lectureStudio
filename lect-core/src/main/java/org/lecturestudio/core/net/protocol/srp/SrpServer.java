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

package org.lecturestudio.core.net.protocol.srp;

import java.math.BigInteger;

import org.lecturestudio.core.crypto.srp.SrpAgreementException;
import org.lecturestudio.core.crypto.srp.SrpContext;
import org.lecturestudio.core.crypto.srp.SrpServerContext;
import org.lecturestudio.core.net.protocol.srp.message.SrpClientEvidenceMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpClientIdentityMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpMessageCode;
import org.lecturestudio.core.net.protocol.srp.message.SrpServerChallengeMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpServerEvidenceMessage;

/**
 * The {@link SrpServer} is a stateful server-side implementation that sends and
 * receives {@link SrpMessage}s during the SRP password-authenticated key  agreement.
 * 
 * @author Alex Andres
 */
public class SrpServer extends SrpNetworkTool {

	/**
	 * Enumerates the states of the server-side SRP-6a authentication.
	 */
	private enum SrpServerState {

		/**
		 * Wait for user identity 'I' and public value 'A'. On successful
		 * receipt the public value 'B' based on the matching password verifier
		 * 'v', the user's random salt 's' and if necessary the group parameters
		 * are sent.
		 */
		ROUND_1,

		/**
		 * Wait for clients evidence message 'M'. On successful receipt and
		 * verification send servers evidence message.
		 */
		ROUND_2,

		/**
		 * Received and verified clients evidence message. The server-side
		 * authentication process is completed.
		 */
		AUTHENTICATED
	}

	/** The current authentication state. */
	private SrpServerState state;

	/** The context that handles SRP related computations. */
	private final SrpServerContext context;


	/**
	 * Creates a new {@link SrpServer} with the provided {@link SrpServerContext}.
	 *
	 * @param context The server-side SRP context.
	 */
	public SrpServer(SrpServerContext context) {
		this.context = context;
		this.state = SrpServerState.ROUND_1;
	}

	@Override
	public void processSrpMessage(SrpMessage message) throws SrpAgreementException {
		if (!isValidState(message)) {
			throw new SrpAgreementException(
					String.format("Invalid message id %d in state %s received.",
							message.getMessageCode(), state.toString()));
		}

		if (state == SrpServerState.ROUND_1) {
			processRound1(message);
		}
		else if (state == SrpServerState.ROUND_2) {
			processRound2(message);
		}
	}

	/**
	 * Get the server-side SRP context.
	 */
	public SrpContext getContext() {
		return context;
	}

	/**
	 * Processes the incoming message in the first round of the agreement.
	 * The message should contain the identity and the public value of the client.
	 * If the public value is not valid an {@link SrpAgreementException} is thrown.
	 * On successful verification the servers public value is created for transmission.
	 *
	 * @param message The incoming {@link SrpMessage}.
	 */
	private void processRound1(SrpMessage message) throws SrpAgreementException {
		SrpClientIdentityMessage identityMessage = (SrpClientIdentityMessage) message;
		String identity = identityMessage.getIdentity();
		BigInteger A = identityMessage.getPublicValue();

		if (identity.isEmpty()) {
			throw new SrpAgreementException("The user identity is empty.");
		}

		if (!context.verifyPublicValue(A)) {
			throw new SrpAgreementException("Clients public value could not be verified.");
		}

		BigInteger salt = context.getSalt();
		BigInteger B = context.computePublicValue();

		context.computeSessionKey(A);

		SrpServerChallengeMessage publicValue = new SrpServerChallengeMessage(B, salt);
		newOutgoingMessage(publicValue);

		state = SrpServerState.ROUND_2;
	}

	/**
	 * Processes the incoming message in the second round of the agreement.
	 * The message should contain the evidence of the client.
	 * On successful verification the servers evidence is created for transmission and
	 * the server-side agreement is completed.
	 * 
	 * @param message The incoming {@link SrpMessage}.
	 */
	private void processRound2(SrpMessage message) throws SrpAgreementException {
		SrpClientEvidenceMessage clientsEvidence = (SrpClientEvidenceMessage) message;
		BigInteger M = clientsEvidence.getEvidence();

		if (!context.verifySessionKey(M)) {
			throw new SrpAgreementException("Clients evidence message could not be verified.");
		}

		BigInteger hAMK = context.computeSessionKeyVerifier();

		SrpServerEvidenceMessage serversEvidence = new SrpServerEvidenceMessage(hAMK);
		newOutgoingMessage(serversEvidence);

		state = SrpServerState.AUTHENTICATED;
	}

	@Override
	protected boolean isValidState(SrpMessage message) {
		SrpMessageCode code = message.getMessageCode();

		switch (state) {
			case ROUND_1:
				return code == SrpMessageCode.CLIENT_IDENTITY;

			case ROUND_2:
				return code == SrpMessageCode.CLIENT_EVIDENCE;

			default:
				return false;
		}
	}

	@Override
	public boolean isAuthenticated() {
		return state == SrpServerState.AUTHENTICATED;
	}

}
