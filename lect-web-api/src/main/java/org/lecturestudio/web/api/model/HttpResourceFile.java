package org.lecturestudio.web.api.model;

import java.util.StringJoiner;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;

@Entity
public class HttpResourceFile {

	@Id
	@SequenceGenerator(name = "HttpResourceFileGen", sequenceName = "http_resource_file_seq", allocationSize = 1)
	@GeneratedValue(generator = "HttpResourceFileGen")
	private long id;

	private String name;

	private long modified;

	@Lob
	private byte[] content;


	public HttpResourceFile() {

	}

	public HttpResourceFile(String name, byte[] content) {
		setName(name);
		setContent(content);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ",
				HttpResourceFile.class.getSimpleName() + "[", "]")
				.add("id=" + id).add("name='" + name + "'")
				.add("length='" + content.length + "'")
				.toString();
	}
}
