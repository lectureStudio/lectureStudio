module org.lecturestudio.javaffmpeg {

	requires java.desktop;
	requires maven.plugin.api;
	requires maven.project;
	requires org.apache.logging.log4j;
	requires slf4j.api;

	exports com.github.javaffmpeg;
	exports org.bytedeco.javacpp;

}