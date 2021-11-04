package de.werum.coprs.nativeapi.service.helper;

import java.net.URL;

public class DownloadUrl {

	private String name;

	private URL url;

	public DownloadUrl() {
		super();
	}

	public DownloadUrl(final String name, final URL url) {
		this.name = name;
		this.url = url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.url == null) ? 0 : this.url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final DownloadUrl other = (DownloadUrl) obj;

		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}

		if (this.url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!this.url.equals(other.url)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [name=" + this.name + ", URL=" + this.url + "]";
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URL getUrl() {
		return this.url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

}
