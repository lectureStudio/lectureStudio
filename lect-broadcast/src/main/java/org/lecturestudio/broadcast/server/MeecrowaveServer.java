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

package org.lecturestudio.broadcast.server;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.net.ApplicationServer;
import org.lecturestudio.core.util.DirUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.broadcast.config.Configuration;
import org.lecturestudio.broadcast.servlet.ApplicationManagerServlet;
import org.lecturestudio.broadcast.servlet.ErrorReportValve;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.meecrowave.Meecrowave;

public class MeecrowaveServer extends ExecutableBase implements ApplicationServer {

	private static final AppDataLocator LOCATOR = new AppDataLocator("lectureBroadcaster");

	private static final String TOMCAT_ROOT = "resources/tomcat";

	private final Configuration config;

	private Meecrowave meecrowave;


	public MeecrowaveServer(Configuration config) {
		this.config = config;
	}

	@Override
	public void startWebApp(String contextPath, String appName) throws Exception {
		if (isNull(meecrowave)) {
			throw new Exception("Server has to be started first.");
		}

		File warFile = new File(toAppDataPath(config.baseDir) + "/apps/" + appName);

		meecrowave.deployWebapp(contextPath, warFile);
	}

	@Override
	public void stopWebApp(String contextPath) throws Exception {
		if (isNull(meecrowave)) {
			throw new Exception("Server has to be started first.");
		}

		contextPath = getContextPath(contextPath);

		meecrowave.undeploy(contextPath);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		try {
			initWebRoot();
		}
		catch (Exception e) {
			throw new ExecutableException("Initialize web-root failed.", e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		String keyAlias = config.keystoreConfig.keyAlias;
		String keystorePassword = config.keystoreConfig.keystorePassword;
		String keystorePath = config.keystoreConfig.keystorePath;
		String keystoreType = config.keystoreConfig.keystoreType;

		Meecrowave.Builder builder = new Meecrowave.Builder();
		builder.setDir(toAppDataPath(config.baseDir));
		builder.setHttpPort(config.port);
		builder.includePackages("org.lecturestudio.web");

		if (config.tlsEnabled) {
			Connector httpsConnector = new Connector();
			httpsConnector.setPort(config.tlsPort);
			httpsConnector.setSecure(true);
			httpsConnector.setScheme("https");
			httpsConnector.setAttribute("sslProtocol", "TLS");
			httpsConnector.setAttribute("SSLEnabled", true);
			httpsConnector.setAttribute("keyAlias", keyAlias);
			httpsConnector.setAttribute("keystorePass", keystorePassword);
			httpsConnector.setAttribute("keystoreFile", keystorePath);
			httpsConnector.setAttribute("keystoreType", keystoreType);

			builder.getConnectors().add(httpsConnector);
		}

		try {
			meecrowave = new Meecrowave(builder);
			meecrowave.bake();

			setErrorReportValve();

			startApplications();
			startApplicationManager();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (nonNull(meecrowave)) {
			try {
				meecrowave.close();
				meecrowave = null;
			}
			catch (Exception e) {
				throw new ExecutableException(e);
			}
		}
	}

	@Override
	protected void destroyInternal() {

	}

	private void setErrorReportValve() throws LifecycleException, IOException {
		StandardHost host = (StandardHost) meecrowave.getTomcat().getHost();
		String errorValve = host.getErrorReportValveClass();

		// Remove default ErrorReportValve.
		for (Valve valve : host.getPipeline().getValves()) {
			if (errorValve.equals(valve.getClass().getName())) {
				host.getPipeline().removeValve(valve);
				break;
			}
		}

		File errorFile = new File(toAppDataPath(config.baseDir) + "/static/error.html");
		String contents = new String(Files.readAllBytes(Paths.get(errorFile.getAbsolutePath())));

		ErrorReportValve errorReportValve = new ErrorReportValve();
		errorReportValve.setErrorTemplate(contents);

		host.getPipeline().addValve(errorReportValve);
		host.setErrorReportValveClass(errorReportValve.getClass().getName());

		// Restart host.
		host.stop();
		host.start();
	}

	private void startApplications() throws Exception {
		for (Map<String, String> serviceMap : config.applications) {
			String path = serviceMap.get("path");
			String name = serviceMap.get("name");

			startWebApp(path, name);
		}
	}

	private void startApplicationManager() {
		String appManagerName = ApplicationManagerServlet.class.getName();

		Context ctx = meecrowave.getTomcat().addContext("/server", null);
		Tomcat.addServlet(ctx, appManagerName, new ApplicationManagerServlet(this));
		ctx.addServletMappingDecoded("/manager", appManagerName);
	}

	private static String getContextPath(String contextPath) {
		if (contextPath.equals("/")) {
			contextPath = "";
		}

		return contextPath;
	}

	private void initWebRoot() throws Exception {
		String baseDir = toAppDataPath(config.baseDir);
		File webRoot = new File(baseDir);

		if (!webRoot.exists()) {
			webRoot.mkdirs();

			copyResourceToFilesystem(TOMCAT_ROOT, webRoot.getAbsolutePath());
		}

		System.setProperty("catalina.base", webRoot.getAbsolutePath());
		System.setProperty("catalina.home", webRoot.getAbsolutePath());
	}

	private void copyResourceToFilesystem(String resName, String baseDir) throws Exception {
		URL resURL = ResourceLoader.getResourceURL(resName);

		if (ResourceLoader.isJarResource(resURL)) {
			String jarPath = ResourceLoader.getJarPath(this.getClass());
			FileUtils.copyJarResource(jarPath, resName, baseDir);
		}
		else {
			File resFile = new File(resURL.getPath());
			Path sourcePath = resFile.toPath();

			if (resFile.isFile()) {
				Path targetPath = Paths.get(baseDir, resFile.getName());
				Files.copy(sourcePath, targetPath);
			}
			else if (resFile.isDirectory()) {
				Path targetPath = Paths.get(baseDir);
				DirUtils.copy(sourcePath, targetPath);
			}
		}
	}

	private String toAppDataPath(String path) {
		return LOCATOR.toAppDataPath(path);
	}
}
