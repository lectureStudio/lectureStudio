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

package org.lecturestudio.core.net.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Enumeration;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import org.lecturestudio.core.util.FileUtils;

public class SslUtils {

	static
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
	
	/**
	 * Loads a {@link KeyStore} which is located in the specified path.
	 * If the {@link KeyStore} exists then the password is needed to open it,
	 * otherwise a new {@link KeyStore} is created.
	 * 
	 * @param ksLocation The path to the KeyStore file.
	 * @param ksPassword The password to open the {@link KeyStore}.
	 * 
	 * @return The loaded {@link KeyStore}.
	 */
	public static KeyStore getKeyStore(String ksLocation, String ksPassword) throws Exception {
		File keystoreFile = new File(ksLocation);
		File parent = keystoreFile.getParentFile();

		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		KeyStore ks = KeyStore.getInstance("PKCS12");

		if (!keystoreFile.exists()) {
			ks.load(null, null);
		}
		else {
			ks.load(new FileInputStream(ksLocation), ksPassword.toCharArray());
		}

		return ks;
	}

	/**
	 * Create a self-signed X.509 Certificate.
	 * 
	 * @param dn The X.509 Distinguished Name.
	 * @param pair The {@link KeyPair}, private and public key.
	 * @param days The number of days the Certificate is valid from now.
	 * @param algorithm The signing algorithm, e.g. "SHA1withRSA".
	 */
	public static X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm) {
		X509Certificate cert = null;
		
		try {
			X500Name issuerDN = new X500Name(dn);
			X500Name subjectDN = new X500Name(dn);
			
			Date notBefore = new Date(System.currentTimeMillis());
			Date notAfter = new Date(System.currentTimeMillis() + (days * 86400000L));
			BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

			X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
					issuerDN, serial, notBefore, notAfter,
					subjectDN, pair.getPublic());
			
			ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(BC).build(pair.getPrivate());
			
			cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));
			cert.checkValidity(new Date());
			cert.verify(cert.getPublicKey());
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Failed to generate self-signed certificate!", t);
		}
		
		return cert;
	}

	/**
	 *
	 * @param ksLocation The {@link KeyStore} location.
	 * @param ksPassword The {@link KeyStore} password.
	 */
	public static void createDefaultKeystore(String ksLocation, String ksPassword) throws Exception {
		File ksFile = new File(ksLocation);

		if (ksFile.exists()) {
			return;
		}

		File parent = ksFile.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		String dn = "C=DE, ST=Germany, L=Darmstadt, O=NONE, OU=NONE, CN=Presenter";

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", BC);
		keyPairGenerator.initialize(2048, new SecureRandom());
		KeyPair KPair = keyPairGenerator.generateKeyPair();

		X509Certificate cert = generateCertificate(dn, KPair, 180, "SHA1withRSA");

		KeyStore ks = getKeyStore(ksLocation, ksPassword);
		ks.setKeyEntry("presenter.cert", KPair.getPrivate(), "cert-password".toCharArray(), new Certificate[] { cert });

		writeKeyStore(ks, ksLocation, ksPassword);
	}

	/**
	 * Creates a new Certificate with the specified parameters.
	 * 
	 * @param DN The X.509 Distinguished Name.
	 * @param certAlias The certificate alias within the {@link KeyStore}.
	 * @param certPassword The Certificate password in the {@link KeyStore}.
	 * @param keyAlgorithm The key algorithm, e.g. RSA.
	 * @param keyLength The key length, e.g. 2048 Bits.
	 * @param signAlgorithm The signing algorithm, e.g. "SHA1withRSA".
	 * @param days The number of days the Certificate is valid from now.
	 * @param ksLocation The {@link KeyStore} location.
	 * @param ksPassword The {@link KeyStore} password.
	 * 
	 * @return The created Certificate.
	 */
	public static Certificate createUserCertificate(String DN, String certAlias, String certPassword,
													String keyAlgorithm, int keyLength, String signAlgorithm, int days,
													String ksLocation, String ksPassword) throws Exception {
		File ksFile = new File(ksLocation);

		if (ksFile.exists()) {
			return null;
		}

		File parent = ksFile.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, BC);
		keyPairGenerator.initialize(keyLength, new SecureRandom());
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		X509Certificate cert = generateCertificate(DN, keyPair, days, signAlgorithm);

		KeyStore ks = getKeyStore(ksLocation, ksPassword);
		ks.setKeyEntry(certAlias, keyPair.getPrivate(), certPassword.toCharArray(), new Certificate[] { cert });

		writeKeyStore(ks, ksLocation, ksPassword);

		return cert;
	}

	/**
	 * Imports a Certificate from a file to a {@link KeyStore} which is found in the specified location.
	 * The Certificate is identified by the alias in the {@link KeyStore}.
	 * The key file is optional.
	 * If the key file is not specified then a new random private key is generated and associated the the Certificate.
	 * 
	 * @param certFile The Certificate file.
	 * @param keyFile The key file.
	 * @param certAlias The certificate alias within the {@link KeyStore}.
	 * @param certPassword The Certificate password in the {@link KeyStore}.
	 * @param ksLocation The {@link KeyStore} location.
	 * @param ksPassword The {@link KeyStore} password.
	 */
	public static void importCertificate(File certFile, File keyFile, String certAlias, String certPassword,
										 String ksLocation, String ksPassword) throws Exception {
		PrivateKey privateKey = null;
		// load Key
		if (keyFile != null && keyFile.exists()) {
			InputStream keyStream = FileUtils.getByteArrayInputStream(keyFile);
			byte[] keyBytes = new byte[keyStream.available()];

			KeyFactory kf = KeyFactory.getInstance("RSA");
			keyStream.read(keyBytes, 0, keyStream.available());
			keyStream.close();

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			privateKey = kf.generatePrivate(keySpec);
		}
		else {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", BC);
			keyPairGenerator.initialize(2048, new SecureRandom());
			privateKey = keyPairGenerator.generateKeyPair().getPrivate();
		}

		// load certificate
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		FileInputStream certStream = new FileInputStream(certFile);
		Certificate cert = cf.generateCertificate(certStream);
		certStream.close();

		Certificate[] chain = { cert };

		KeyStore ks = getKeyStore(ksLocation, ksPassword);
		ks.setKeyEntry(certAlias, privateKey, certPassword.toCharArray(), chain);

		writeKeyStore(ks, ksLocation, ksPassword);

		// printDetails(ksLocation, ksPassword);
	}

	/**
	 * Imports a Certificate from a file to a {@link KeyStore} which is found in the specified location.
	 * The Certificate is identified by the alias in the {@link KeyStore}.
	 * If a {@link KeyStore} could not be loaded,
	 * then a new {@link KeyStore} is created which contains the new Certificate.
	 * 
	 * @param certFile The file in which the certificate is stored.
	 * @param certAlias The certificate alias within the {@link KeyStore}.
	 * @param ksLocation The location of the {@link KeyStore}.
	 * @param ksPassword The {@link KeyStore} password.
	 */
	public static void importCertificate(File certFile, String certAlias, String ksLocation, String ksPassword)
			throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		FileInputStream certStream = new FileInputStream(certFile);
		Certificate cert = cf.generateCertificate(certStream);
		certStream.close();

		KeyStore ks = getKeyStore(ksLocation, ksPassword);
		ks.setCertificateEntry(certAlias, cert);

		writeKeyStore(ks, ksLocation, ksPassword);

		// printDetails(ksLocation, ksPassword);
	}

	/**
	 * Exports the provided Certificate to the specified location.
	 * 
	 * @param cert A Certificate.
	 * @param location The location where the Certificate should be stored.
	 */
	public static void exportCertificate(Certificate cert, String location) throws Exception {
		FileOutputStream fos = new FileOutputStream(location);
		fos.write(cert.getEncoded());
		fos.close();
	}

	/**
	 * Writes a previously created {@link KeyStore} to a file.
	 * 
	 * @param ks The {@link KeyStore} to write.
	 * @param ksLocation The new location of the {@link KeyStore}.
	 * @param ksPassword The {@link KeyStore} password.
	 */
	public static void writeKeyStore(KeyStore ks, String ksLocation, String ksPassword) throws Exception {
		FileOutputStream out = new FileOutputStream(ksLocation);
		ks.store(out, ksPassword.toCharArray());
		out.close();
	}

	/**
	 * Prints the certificates within a {@link KeyStore}.
	 * 
	 * @param ksLocation The location of the {@link KeyStore} file.
	 * @param ksPassword The {@link KeyStore} password.
	 */
	public static void printDetails(String ksLocation, String ksPassword) {
		try {
			KeyStore ks = getKeyStore(ksLocation, ksPassword);

			System.out.println(ks.aliases().hasMoreElements());

			Enumeration<String> e = ks.aliases();
			while (e.hasMoreElements()) {
				String alias = (String) e.nextElement();
				Certificate cert = ks.getCertificate(alias);

				System.out.println(alias);

				if (cert instanceof X509Certificate) {
					X509Certificate x509cert = (X509Certificate) cert;

					System.out.println(x509cert.toString());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
