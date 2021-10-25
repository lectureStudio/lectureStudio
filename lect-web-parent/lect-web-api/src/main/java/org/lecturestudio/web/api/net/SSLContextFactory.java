/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.net;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLContextFactory {

	public static SSLContext createSSLContext() {
		SSLContext sslContext;

		try {
			X509TrustManager trustManager = new OwnTrustManager("keystore.jks",
					"mypassword");

			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, new TrustManager[] { trustManager }, null);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return sslContext;
	}

}
