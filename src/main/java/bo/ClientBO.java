package bo;

import java.sql.Timestamp;

import bean.Clients;
import dao.ClientDAO;

public class ClientBO {
	private ClientDAO clientDAO;
	public ClientBO() {
		this.clientDAO = new ClientDAO();
	}
	
	public Clients getById(int id) {
		return clientDAO.getById(id);
	}
	
	public Clients getByName(String username) {
		return clientDAO.getByName(username);
	}
	
	public boolean addClient(String username, String password, String image) {
		Clients client = new Clients(0, username, password, image, new Timestamp(System.currentTimeMillis()));
		return clientDAO.addClient(client);
	}
}
