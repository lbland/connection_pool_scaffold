package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionWrapperImplementation implements ConnectionWrapper {

	// private members	
	private DataSource dataSource;

	
	public ConnectionWrapperImplementation(DataSource datasource) {
		this.dataSource = datasource;
	}

	/**
	 * Gets a connection from the data source
	 * <p>
	 * 
	 * 
	 * @return a connection 
	 * @throws SQLException
	 */
	public Connection getConnection( ) throws SQLException {
		return this.dataSource.getConnection();	
	}
	
}
