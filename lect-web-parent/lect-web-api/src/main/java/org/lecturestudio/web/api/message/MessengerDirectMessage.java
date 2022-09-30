package org.lecturestudio.web.api.message;

import static java.util.Objects.requireNonNullElse;

public class MessengerDirectMessage extends MessengerMessage {

	private String recipientId;

	private String recipientFirstName;

	private String recipientFamilyName;


	public MessengerDirectMessage() {

	}

	public MessengerDirectMessage(MessengerDirectMessage other) {
		setUserId(other.getUserId());
		setFirstName(other.getFirstName());
		setFamilyName(other.getFamilyName());
		setRecipientId(other.getRecipientId());
		setRecipientFirstName(other.getRecipientFirstName());
		setRecipientFamilyName(other.getRecipientFamilyName());
		setMessage(other.getMessage());
		setDate(other.getDate());
		setMessageId(other.getMessageId());
	}

	public String getRecipientFirstName() {
		return requireNonNullElse(recipientFirstName, "");
	}

	public void setRecipientFirstName(String firstName) {
		recipientFirstName = firstName;
	}

	public String getRecipientFamilyName() {
		return requireNonNullElse(recipientFamilyName, "");
	}

	public void setRecipientFamilyName(String familyName) {
		recipientFamilyName = familyName;
	}

	public String getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(String id) {
		recipientId = id;
	}
}