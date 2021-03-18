package com.github.javaffmpeg;

public class OsInfo {

	private static String osName;

	private static String osArch;

	private static String platformName;


	static {
		osName = System.getProperty("os.name").toLowerCase();
		osArch = System.getProperty("os.arch").toLowerCase();
		String jvmName = System.getProperty("java.vm.name").toLowerCase();

		if (jvmName.startsWith("dalvik") && osName.startsWith("linux")) {
			osName = "android";
		}
		else if (osName.startsWith("mac os x")) {
			osName = "macosx";
		}
		else {
			osName = osName.split(" ")[0];
		}

		if (osArch.endsWith("86")) {
			osArch = "x86";
		}
		else if (osArch.equals("amd64") || osArch.equals("x86-64")) {
			osArch = "x86_64";
		}
		else if (osArch.startsWith("arm")) {
			osArch = "arm";
		}

		platformName = osName + "-" + osArch;
	}
	

	private OsInfo() {
	}

	public static boolean isLinux() {
		return osName.startsWith("linux");
	}

	public static boolean isMac() {
		return osName.startsWith("mac");
	}

	public static boolean isWindows() {
		return osName.startsWith("windows");
	}

	public static String getPlatformName() {
		return platformName;
	}

}
