package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class ConnectionWrapperImplementation implements ConnectionWrapper {

	// private members
	private String url;
	private String user;
	private String password;

	/**
	 * 
	 * @param url
	 * @param user
	 * @param password
	 */
	
	public ConnectionWrapperImplementation(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	/**
	 * Gets a connection to the url.
	 * <p>
	 * 
	 * 
	 * @return a connection to the url.
	 * @throws SQLException
	 */
	public Connection getConnection( ) throws SQLException {
		return DriverManager.getConnection(this.url, this.user, this.password);
	}
	
}
