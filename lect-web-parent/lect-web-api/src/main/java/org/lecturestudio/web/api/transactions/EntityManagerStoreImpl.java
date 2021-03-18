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

import java.util.Stack;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A store for entity managers. It is basically a ThreadLocal which stores the entity manager.
 * The {@link TransactionInterceptor} is expected to register entity manager. The application code
 * can get the current entity manager either by injecting the store or the {@link EntityManagerDelegate}.
 */
@ApplicationScoped
public class EntityManagerStoreImpl implements EntityManagerStore {

	private final Logger logger = LogManager.getLogger(EntityManagerStoreImpl.class);

	private final ThreadLocal<Stack<EntityManager>> emStackThreadLocal = new ThreadLocal<>();

	private final EntityManagerFactory emf;


	public EntityManagerStoreImpl() {
		emf = Persistence.createEntityManagerFactory("Webservice");
	}

	public void init(@Observes @Initialized(RequestScoped.class) Object init) {
		createAndRegister();
	}

	public void destroy(@Observes @Destroyed(RequestScoped.class) Object init) {
		unregister(get());
	}

	@Override
	public EntityManager get() {
		final Stack<EntityManager> entityManagerStack = emStackThreadLocal.get();

		if (entityManagerStack == null || entityManagerStack.isEmpty()) {
         /*
         if nothing is found, we return null to cause a NullPointer exception in the business code.
         This leeds to a nicer stack trace starting with client code.
          */

			logger.warn("No entity manager was found. Did you forget to mark your method as transactional?");
			return null;
		}

		return entityManagerStack.peek();
	}

	@Override
	public EntityManager createAndRegister() {
		Stack<EntityManager> entityManagerStack = emStackThreadLocal.get();

		if (entityManagerStack == null) {
			entityManagerStack = new Stack<>();
			emStackThreadLocal.set(entityManagerStack);
		}

		final EntityManager entityManager = emf.createEntityManager();
		entityManagerStack.push(entityManager);

		return entityManager;
	}

	@Override
	public void unregister(EntityManager entityManager) {
		final Stack<EntityManager> entityManagerStack = emStackThreadLocal.get();

		if (entityManagerStack == null || entityManagerStack.isEmpty()) {
			throw new IllegalStateException(
					"Removing of entity manager failed. Your entity manager was not found.");
		}

		if (entityManagerStack.peek() != entityManager) {
			throw new IllegalStateException(
					"Removing of entity manager failed. Your entity manager was not found.");
		}

		entityManagerStack.pop();
	}
}
