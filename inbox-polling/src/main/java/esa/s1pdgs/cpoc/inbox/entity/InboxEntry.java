package esa.s1pdgs.cpoc.inbox.entity;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class InboxEntry {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	private String name;
	private String url;
	
	public InboxEntry() {}
	
	public InboxEntry(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(url);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(getClass()))
		{
			final InboxEntry other = (InboxEntry) obj;
			return Objects.equals(url, other.url);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "InboxEntry [id=" + id + ", name=" + name + ", url=" + url + "]";
	}
}
