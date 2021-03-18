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

package org.lecturestudio.web.api.data;

import javax.enterprise.inject.Default;
import javax.persistence.TypedQuery;

import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;

@Default
public class ClassroomDatabaseService extends DatabaseServiceBase<Classroom> implements ClassroomDataService {

	@Override
	public Classroom getByContextPath(String path) {
		String ql = "Select c from Classroom c where c.shortName = :path";
		TypedQuery<Classroom> query = entityManager.createQuery(ql, Classroom.class);
		query.setParameter("path", path);

		return query.getResultList().stream().findFirst().orElse(null);
	}

	@Override
	public <T extends ClassroomService> T getServiceByContextPath(String path, Class<T> serviceClass) {
		String ql = "Select distinct s from Classroom c inner join c.services s where c.shortName = :path and s.type = :type";
		TypedQuery<T> query = entityManager.createQuery(ql, serviceClass);
		query.setParameter("path", path);
		query.setParameter("type", serviceClass.getSimpleName());

		return (T) query.getResultList().stream().findFirst().orElse(null);
	}

	@Override
	public <T extends ClassroomService> T getServiceById(String serviceId, Class<T> serviceClass) {
		String ql = "Select s from ClassroomService s where s.serviceId = :serviceId";
		TypedQuery<T> query = entityManager.createQuery(ql, serviceClass);
		query.setParameter("serviceId", serviceId);

		return query.getResultList().stream().findFirst().orElse(null);
	}

}
