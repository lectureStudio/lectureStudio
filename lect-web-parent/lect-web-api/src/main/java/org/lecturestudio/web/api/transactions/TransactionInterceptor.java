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

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple transaction interceptor which registers an entity mangager in a ThreadLocal and unregisters after the
 * method was called.
 * It does not support any kind of context propagation. If a transactional method calls another's bean transactional
 * method a new entity manager is created and added to the stack.
 */
@Transactional
@Interceptor
public class TransactionInterceptor {

	private static final Logger LOG = LogManager.getLogger(TransactionInterceptor.class);

	@Inject
	private EntityManagerStoreImpl entityManagerStore;


	@AroundInvoke
	public Object runInTransaction(InvocationContext invocationContext) throws Exception {
		LOG.debug("Run transaction");

		EntityManager em = entityManagerStore.createAndRegister();
		Object result;

		try {
			em.getTransaction().begin();

			result = invocationContext.proceed();

			em.getTransaction().commit();
		}
		catch (Exception e) {
			try {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
					LOG.debug("Rolled back transaction");
				}
			}
			catch (Exception e1) {
				LOG.error("Rollback of transaction failed." + e1);
			}

			throw e;
		}
		finally {
			if (em != null) {
				entityManagerStore.unregister(em);
				em.close();
			}
		}

		return result;
	}

}
