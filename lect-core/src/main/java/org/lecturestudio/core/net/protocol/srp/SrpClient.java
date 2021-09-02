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
import org.lecturestudio.core.crypto.srp.SrpClientContext;
import org.lecturestudio.core.net.protocol.srp.message.SrpClientEvidenceMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpClientIdentityMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpMessageCode;
import org.lecturestudio.core.net.protocol.srp.message.SrpServerChallengeMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpServerEvidenceMessage;

/**
 * The {@link SrpClient} is a stateful client-side implementation that sends
 * and receives {@link SrpMessage}s during the SRP password-authenticated key agreement.
 * 
 * @author Alex Andres
 */
public class SrpClient extends SrpNetworkTool {

	/**
	 * Enumerates the states of the client-side SRP-6a authentication.
	 */
	private enum SrpClientState {

		/**
		 * The user identity 'I' and password 'P' are provided by the user.
		 * Sending user identity 'I' and public value 'A' to the server.
		 * Awaiting servers public value 'B' and random salt 's'.
		 */
		ROUND_1,

		/**
		 * Received servers public value 'B' and random salt 's' as response.
		 * Sending evidence message 'M'. Wait for servers evidence message.
		 */
		ROUND_2,

		/**
		 * Received and verified servers evidence message. The client-side
		 * authentication process is completed.
		 */
		AUTHENTICATED

	}

	/** The current authentication state. */
	private SrpClientState state;

	/** The context that handles SRP related computations. */
	private final SrpClientContext context;


	/**
	 * Creates a new SrpClient with the provided {@link SrpClientContext}.
	 * <p>
	 * NOTE: On creation the {@link SrpClientIdentityMessage} is created and put onto the message stack.
	 * 
	 * @param context The client-side SRP context.
	 */
	public SrpClient(SrpClientContext context) {
		this.context = context;
		this.state = SrpClientState.ROUND_1;

		String identity = context.getIdentity();
		BigInteger A = context.computePublicValue();
		SrpClientIdentityMessage idMessage = new SrpClientIdentityMessage(identity, A);

		newOutgoingMessage(idMessage);
	}

	@Override
	public void processSrpMessage(SrpMessage message) throws SrpAgreementException {
		if (!isValidState(message)) {
			throw new SrpAgreementException(
					String.format("Invalid message %d in state %s received.",
							message.getMessageCode(), state.toString()));
		}

		if (state == SrpClientState.ROUND_1) {
			processRound1(message);
		}
		else if (state == SrpClientState.ROUND_2) {
			processRound2(message);
		}
	}

	/**
	 * Processes the incoming message in the first round of the agreement.
	 * The message should contain the public value of the server.
	 * If the public value is not valid an {@link SrpAgreementException} is thrown.
	 * On successful verification the clients evidence message is created for transmission.
	 * 
	 * @param message The incoming {@link SrpMessage}.
	 */
	private void processRound1(SrpMessage message) throws SrpAgreementException {
		SrpServerChallengeMessage serversPublicValue = (SrpServerChallengeMessage) message;

		BigInteger B = serversPublicValue.getPublicValue();
		BigInteger s = serversPublicValue.getSalt();

		if (!context.verifyPublicValue(B)) {
			throw new SrpAgreementException("Servers public value could not be verified.");
		}

		context.computeSessionKey(B, s);
		BigInteger M = context.computeEvidenceMessage();

		SrpClientEvidenceMessage clientsEvidence = new SrpClientEvidenceMessage(M);

		newOutgoingMessage(clientsEvidence);

		state = SrpClientState.ROUND_2;
	}

	/**
	 * Processes the incoming message in the second round of the agreement.
	 * The message should contain the evidence of the server.
	 * On successful verification the agreement is completed.
	 * 
	 * @param message The incoming {@link SrpMessage}.
	 */
	private void processRound2(SrpMessage message) throws SrpAgreementException {
		SrpServerEvidenceMessage evidenceMessage = (SrpServerEvidenceMessage) message;

		BigInteger hAMK = evidenceMessage.getEvidence();

		if (!context.verifySessionKey(hAMK)) {
			throw new SrpAgreementException("Servers evidence message could not be verified.");
		}

		state = SrpClientState.AUTHENTICATED;
	}

	@Override
	protected boolean isValidState(SrpMessage message) {
		SrpMessageCode code = message.getMessageCode();

		switch (state) {
			case ROUND_1:
				return code == SrpMessageCode.SERVER_CHALLENGE;

			case ROUND_2:
				return code == SrpMessageCode.SERVER_EVIDENCE;

			default:
				return false;
		}
	}

	@Override
	public boolean isAuthenticated() {
		return state == SrpClientState.AUTHENTICATED;
	}

}
