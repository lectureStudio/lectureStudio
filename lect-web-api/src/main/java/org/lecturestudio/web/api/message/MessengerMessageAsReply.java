package org.lecturestudio.web.api.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessengerMessageAsReply extends MessengerMessage {

    private String msgIdToReplyTo;

}
