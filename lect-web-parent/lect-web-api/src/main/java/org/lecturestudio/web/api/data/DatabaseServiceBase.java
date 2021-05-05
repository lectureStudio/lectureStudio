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

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public abstract class DatabaseServiceBase<E> implements DataService<E> {

	@Inject
	protected EntityManager entityManager;

	protected Class<E> entityClass;


	DatabaseServiceBase(Class<E> cls) {
		this.entityClass = cls;
	}

	public void add(E entity) {
		entityManager.persist(entity);
	}

	public void update(E entity) {
		entityManager.merge(entity);
	}

	public void delete(E entity) {
		entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
	}

	public void deleteAll() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaDelete<E> query = criteriaBuilder.createCriteriaDelete(entityClass);
		query.from(entityClass);

		entityManager.createQuery(query).executeUpdate();
	}

	public List<E> getAll() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		Root<E> from = criteriaQuery.from(entityClass);
		CriteriaQuery<E> select = criteriaQuery.select(from);
		TypedQuery<E> query = entityManager.createQuery(select);

		return query.getResultList();
	}

	@Override
	public E getById(long id) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		Root<E> from = criteriaQuery.from(entityClass);
		criteriaQuery.where(criteriaBuilder.equal(from.get("id"), id));
		TypedQuery<E> query = entityManager.createQuery(criteriaQuery);

		return query.getSingleResult();
	}

	public Iterator<E> iterator(long first, long count) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		CriteriaQuery<E> select = criteriaQuery.select(criteriaQuery.from(entityClass));
		TypedQuery<E> query = entityManager.createQuery(select);
		query.setFirstResult((int) first);
		query.setMaxResults((int) count);

		return query.getResultList().iterator();
	}

	public long size() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		countQuery.select(criteriaBuilder.count(countQuery.from(entityClass)));

		return entityManager.createQuery(countQuery).getSingleResult();
	}
}
