package org.lecturestudio.web.api.message.util;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.web.api.message.MessengerDirectMessageAsReply;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.MessengerMessageAsReply;
import org.lecturestudio.web.api.message.UserMessage;
import org.lecturestudio.web.api.model.UserInfo;

import java.util.List;
import java.util.Objects;

public class MessageUtil {

    public static boolean isReply(final UserMessage message) {
        return message instanceof MessengerMessageAsReply || message instanceof MessengerDirectMessageAsReply;
    }

    public static String evaluateSender(final UserMessage message, final UserInfo userInfo, final Dictionary dictionary) {
        final boolean byMe = sentByMe(message, userInfo);

        if (byMe) return dictionary.get("text.message.me");
        else {
            String nameFull = message.getFirstName() + " " + message.getFamilyName();
            String[] nameParts = nameFull.split(" ");
            String firstName = nameParts.length > 0 ? nameParts[0] : "";
            String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";

            return String.format("%s %s", firstName, lastName);
        }
    }

    public static String evaluateSenderOfMessageToReplyTo(final UserMessage messageToReplyTo, final UserInfo userInfo, final Dictionary dictionary) {
        final boolean byMe = sentByMe(messageToReplyTo, userInfo);

        if(byMe) return dictionary.get("text.message.me.dative");

        return evaluateSender(messageToReplyTo, userInfo, dictionary);
    }

    public static MessengerMessage findMessageToReplyTo(final List<MessengerMessage> messages, final MessengerMessage message) {
        if(message instanceof MessengerMessageAsReply messageAsReply) {
            return messages.stream()
                    .filter(messengerMessage -> messengerMessage.getMessageId().equals(messageAsReply.getMsgIdToReplyTo()))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Given ID of message to reply to couldn't be found"));
        }

        if(message instanceof MessengerDirectMessageAsReply directMessageAsReply) {
            return messages.stream()
                    .filter(messengerMessage -> messengerMessage.getMessageId().equals(directMessageAsReply.getMsgIdToReplyTo()))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Given ID of message to reply to couldn't be found"));
        }

        return null;
    }

    public static void updateOutdatedMessage(final List<MessengerMessage> messages, final MessengerMessage newMessage) {
		for (MessengerMessage message : messages) {
			if (message.getMessageId().equals(newMessage.getMessageId())) {
				messages.remove(message);
				messages.add(newMessage);
			}
		}
    }

    private static boolean sentByMe(final UserMessage message, final UserInfo userInfo) {
        final String myId = userInfo.getUserId();
        return Objects.equals(message.getUserId(), myId);
    }
}
