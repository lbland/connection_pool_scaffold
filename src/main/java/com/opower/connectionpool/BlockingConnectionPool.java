package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * This class implements a Blocking Connection Pool.
 * if a request for a conncetion comes in when there are no available connections, the request will block
 * until there is a connection available.
 * 
 * @author loren_bland
 * @version 1.0
 */

public class BlockingConnectionPool implements ConnectionPool {

	/**
	 * Private members
	 */
	
	// url, user and password are the connection properties for the connections in the pool
	private String url;
	private String user;
	private String password;
	private ConnectionWrapper connectionWrapper;

	// queue of available connections and used connections
	// Used ArrayList because comparing and removing elements is easier than a queue.
	private ArrayList<Connection> availableConnections;
	private ArrayList<Connection> usedConnections;

	// a lock for accessing the queues
	private final Lock arrayListLock;

	// The Semaphore is used to control access to the connection pool
	// this makes it a blocking call to get a connection
	private final Semaphore arrayListSemaphore; 

	// the size of the pool
	private Integer poolSize;
	
	public BlockingConnectionPool(Integer size, String url, String user, String password) throws SQLException {
		this.poolSize = size;
		this.url = url;
		this.user = user;
		this.password = password;
		this.connectionWrapper = new ConnectionWrapperImplementation(url, user, password);

		this.availableConnections = new ArrayList<Connection>( );
		this.usedConnections = new ArrayList<Connection>( );

		this.arrayListLock = new ReentrantLock();

		this.arrayListSemaphore = new Semaphore(size, true);
		
	}
	
	
	/**
	 * This will return an available connection or block until a connection is available.
	 * <p>
	 * If a connection is not available, this call will block
	 * 
	 * @throws SQLException
	 */
	@Override
	public Connection getConnection( ) throws SQLException {
		return null;
	}
	
	
	/**
	 * adds the connection back to the queue of available connections
	 * 
	 * @throws SQLException
	 */
	@Override
	public void releaseConnection(Connection connection) throws SQLException {

		
	
	}
}
