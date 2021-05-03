package org.lecturestudio.web.api.service;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.lecturestudio.web.api.client.ClassroomProviderClient;
import org.lecturestudio.web.api.model.Classroom;

@Dependent
public class ClassroomProviderService extends ProviderService {

	private final ClassroomProviderClient providerClient;


	@Inject
	public ClassroomProviderService(ServiceParameters parameters) {
		providerClient = createProxy(ClassroomProviderClient.class, parameters);
	}

	public String createClassroom(Classroom classroom) {
		return providerClient.createClassroom(classroom);
	}

	public void updateClassroom(Classroom classroom) {
		providerClient.updateClassroom(classroom);
	}

	public void deleteClassroom(String classroomId) {
		providerClient.deleteClassroom(classroomId);
	}

	public Classroom getClassroom() {
		return providerClient.getClassroom();
	}

	public List<Classroom> getClassrooms() {
		return providerClient.getClassrooms();
	}
}
