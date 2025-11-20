package bean;

import java.sql.Timestamp;

public class Clients {
	int client_id;
	String password_hash;
	String username;
	String image;	
	Timestamp create_at;
	public Clients(int id, String name, String password, String image, Timestamp created) {
		// TODO Auto-generated constructor stub
		this.client_id = id;
		this.username = name;
		this.password_hash = password;
		this.image = image;
		this.create_at = created;
	}
	public int getClient_id() {
		return client_id;
	}
	public void setClient_id(int client_id) {
		this.client_id = client_id;
	}
	public String getPassword_hash() {
		return password_hash;
	}
	public void setPassword_hash(String password_hash) {
		this.password_hash = password_hash;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public Timestamp getCreate_at() {
		return create_at;
	}
	public void setCreate_at(Timestamp create_at) {
		this.create_at = create_at;
	}
}
