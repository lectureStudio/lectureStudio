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

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.lecturestudio.core.io.ResourceLoader;

/**
 * Custom X509TrustManager implementation to be used for custom SSL trust
 * decisions. This trust manager uses both the system trust chain, and
 * self-signed certificates.
 *
 * @author Alex Andres
 */
public class OwnTrustManager implements X509TrustManager {

	private final X509TrustManager defaultTrustManager;

	private final X509TrustManager ownTrustManager;


	/**
	 * Creates a new {@code OwnTrustManager}.
	 *
	 * @param keystorePath The keystore path where to find self-signed
	 *                     certificates.
	 * @param keystorePass The password for the keystore.
	 *
	 * @throws Exception If the trust managers could not be loaded.
	 */
	public OwnTrustManager(String keystorePath, String keystorePass) throws Exception {
		defaultTrustManager = getDefaultTrustManager();
		ownTrustManager = getOwnTrustManager(keystorePath, keystorePass);
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		defaultTrustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		try {
			ownTrustManager.checkServerTrusted(chain, authType);
		}
		catch (CertificateException e) {
			defaultTrustManager.checkServerTrusted(chain, authType);
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return defaultTrustManager.getAcceptedIssuers();
	}

	/**
	 * Initialises the TrustManagerFactory with the default trust store and
	 * retrieves the X509TrustManager.
	 *
	 * @return the default trust manager.
	 */
	private X509TrustManager getDefaultTrustManager() throws Exception {
		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init((KeyStore) null);

		for (TrustManager trustManager : tmf.getTrustManagers()) {
			if (trustManager instanceof X509TrustManager) {
				return (X509TrustManager) trustManager;
			}
		}

		throw new IllegalStateException("Cannot load a X509TrustManager");
	}

	private X509TrustManager getOwnTrustManager(String keystorePath,
			String keystorePass) throws Exception {
		KeyStore trustStore;

		try (InputStream keyStream = ResourceLoader.getResourceAsStream(keystorePath)) {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(keyStream, keystorePass.toCharArray());
		}

		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);

		for (TrustManager trustManager : tmf.getTrustManagers()) {
			if (trustManager instanceof X509TrustManager) {
				return (X509TrustManager) trustManager;
			}
		}

		throw new IllegalStateException("Cannot load a X509TrustManager");
	}
}
