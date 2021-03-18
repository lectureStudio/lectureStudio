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

package org.lecturestudio.web.api.transactions;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

@ApplicationScoped
public class EntityManagerDelegate implements EntityManager {

	@Inject
	private EntityManagerStore entityManagerStore;


	@Override
	public void persist(Object entity) {
		entityManagerStore.get().persist(entity);
	}

	@Override
	public <T> T merge(T entity) {
		return entityManagerStore.get().merge(entity);
	}

	@Override
	public void remove(Object entity) {
		entityManagerStore.get().remove(entity);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return entityManagerStore.get().find(entityClass, primaryKey);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		return entityManagerStore.get().find(entityClass, primaryKey, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		return entityManagerStore.get().find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		return entityManagerStore.get().find(entityClass, primaryKey, lockMode, properties);
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		return entityManagerStore.get().getReference(entityClass, primaryKey);
	}

	@Override
	public void flush() {
		entityManagerStore.get().flush();
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		entityManagerStore.get().setFlushMode(flushMode);
	}

	@Override
	public FlushModeType getFlushMode() {
		return entityManagerStore.get().getFlushMode();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {
		entityManagerStore.get().lock(entity, lockMode);
	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		entityManagerStore.get().lock(entity, lockMode, properties);
	}

	@Override
	public void refresh(Object entity) {
		entityManagerStore.get().refresh(entity);
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		entityManagerStore.get().refresh(entity, properties);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		entityManagerStore.get().refresh(entity, lockMode);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		entityManagerStore.get().refresh(entity, lockMode, properties);
	}

	@Override
	public void clear() {
		entityManagerStore.get().clear();
	}

	@Override
	public void detach(Object entity) {
		entityManagerStore.get().detach(entity);
	}

	@Override
	public boolean contains(Object entity) {
		return entityManagerStore.get().contains(entity);
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		return entityManagerStore.get().getLockMode(entity);
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		entityManagerStore.get().setProperty(propertyName, value);
	}

	@Override
	public Map<String, Object> getProperties() {
		return entityManagerStore.get().getProperties();
	}

	@Override
	public Query createQuery(String qlString) {
		return entityManagerStore.get().createQuery(qlString);
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return entityManagerStore.get().createQuery(criteriaQuery);
	}

	@Override
	public Query createQuery(CriteriaUpdate criteriaUpdate) {
		return entityManagerStore.get().createQuery(criteriaUpdate);
	}

	@Override
	public Query createQuery(CriteriaDelete criteriaDelete) {
		return entityManagerStore.get().createQuery(criteriaDelete);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return entityManagerStore.get().createQuery(qlString, resultClass);
	}

	@Override
	public Query createNamedQuery(String name) {
		return entityManagerStore.get().createNamedQuery(name);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return entityManagerStore.get().createNamedQuery(name, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		return entityManagerStore.get().createNativeQuery(sqlString);
	}

	@Override
	public Query createNativeQuery(String sqlString, Class resultClass) {
		return entityManagerStore.get().createNativeQuery(sqlString, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		return entityManagerStore.get().createNativeQuery(sqlString, resultSetMapping);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		return entityManagerStore.get().createNamedStoredProcedureQuery(name);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		return entityManagerStore.get().createStoredProcedureQuery(procedureName);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
		return entityManagerStore.get().createStoredProcedureQuery(procedureName, resultClasses);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		return entityManagerStore.get().createStoredProcedureQuery(procedureName, resultSetMappings);
	}

	@Override
	public void joinTransaction() {
		entityManagerStore.get().joinTransaction();
	}

	@Override
	public boolean isJoinedToTransaction() {
		return entityManagerStore.get().isJoinedToTransaction();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return entityManagerStore.get().unwrap(cls);
	}

	@Override
	public Object getDelegate() {
		return entityManagerStore.get().getDelegate();
	}

	@Override
	public void close() {
		entityManagerStore.get().close();
	}

	@Override
	public boolean isOpen() {
		return entityManagerStore.get().isOpen();
	}

	@Override
	public EntityTransaction getTransaction() {
		return entityManagerStore.get().getTransaction();
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerStore.get().getEntityManagerFactory();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return entityManagerStore.get().getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel() {
		return entityManagerStore.get().getMetamodel();
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		return entityManagerStore.get().createEntityGraph(rootType);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName) {
		return entityManagerStore.get().createEntityGraph(graphName);
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName) {
		return entityManagerStore.get().getEntityGraph(graphName);
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		return entityManagerStore.get().getEntityGraphs(entityClass);
	}
}
