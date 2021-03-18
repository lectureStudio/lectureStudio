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

package org.lecturestudio.web.auth.service.classroom;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import org.lecturestudio.web.auth.model.UserAccount;
import org.lecturestudio.web.auth.model.UserAccounts;
import org.lecturestudio.web.auth.service.AuthApplication;
import org.lecturestudio.web.auth.service.oauth2.OAuth2DataProvider;
import org.lecturestudio.web.auth.service.oauth2.OAuth2RequestFilter;

@ApplicationPath("/classroom")
@ApplicationScoped
public class ClassroomApplication extends AuthApplication {

	@Inject
	private OAuth2DataProvider manager;

	@Inject
	private UserAccounts accounts;


	@Override
	public Set<Object> getSingletons() {
		Set<Object> classes = new HashSet<>();

		UserAccount adminAccount = new UserAccount("admin", "admin", "admin");
		adminAccount.getRoles().add("lecturer");
		adminAccount.getRoles().add("student");

		accounts.setAccount(adminAccount);
		accounts.setAccount(new UserAccount("consumer-id", "consumer-id", "this-is-a-secret"));



		OAuth2RequestFilter requestFilter = new OAuth2RequestFilter();
		requestFilter.setDataProvider(manager);

		ClassroomAccountService accountService = new ClassroomAccountService();
		accountService.setAccounts(accounts);

//		classes.add(requestFilter);
		classes.add(accountService);

		return classes;
	}
}
