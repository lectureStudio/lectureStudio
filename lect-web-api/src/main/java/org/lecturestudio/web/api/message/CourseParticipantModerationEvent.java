package org.lecturestudio.web.api.message;

import lombok.Getter;
import lombok.Setter;
import org.lecturestudio.web.api.stream.model.ModerationType;

@Getter
@Setter
public final class CourseParticipantModerationEvent extends UserMessage {
	ModerationType moderationType;
}
