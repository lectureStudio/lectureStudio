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

import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;

@Dependent
public class ClassroomDatabaseService extends DatabaseServiceBase<Classroom> implements ClassroomDataService {

	@Override
	public Classroom getByContextPath(String path) {
		String ql = "Select c from Classroom c where c.shortName = :path";
		TypedQuery<Classroom> query = entityManager.createQuery(ql, Classroom.class);
		query.setParameter("path", path);

		return query.getResultList().stream().findFirst().orElse(null);
	}

	@Override
	public Classroom getByUuid(UUID uuid) {
		String ql = "Select c from Classroom c where c.uuid = :uuid";
		TypedQuery<Classroom> query = entityManager.createQuery(ql, Classroom.class);
		query.setParameter("uuid", uuid);

		return query.getResultList().stream().findFirst().orElse(null);
	}

	@Override
	public <T extends ClassroomService> T getServiceByContextPath(String path, Class<T> serviceClass) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(serviceClass);
		Root<T> fromType = criteriaQuery.from(serviceClass);
		Path<Object> contextPath = fromType.get("contextPath");

		CriteriaQuery<T> select = criteriaQuery.select(fromType);
		select.where(builder.equal(contextPath, path));

		TypedQuery<T> query = entityManager.createQuery(select);

		return query.getResultList().stream().findFirst().orElse(null);
	}

	@Override
	public <T extends ClassroomService> T getServiceById(String serviceId, Class<T> serviceClass) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(serviceClass);
		Root<T> fromType = criteriaQuery.from(serviceClass);
		Path<Object> serviceIdPath = fromType.get("serviceId");

		CriteriaQuery<T> select = criteriaQuery.select(fromType);
		select.where(builder.equal(serviceIdPath, serviceId));

		TypedQuery<T> query = entityManager.createQuery(select);

		return query.getResultList().stream().findFirst().orElse(null);
	}

}
