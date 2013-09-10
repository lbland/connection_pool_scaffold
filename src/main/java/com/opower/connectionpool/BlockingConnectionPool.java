package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
/**
 * This class implements a Blocking Connection Pool.
 * if a request for a connection comes in when there are no available connections, the request will block
 * until there is a connection available.
 * 
 * @author loren_bland
 * @version 1.0
 */

public class BlockingConnectionPool implements ConnectionPool {

	/**
	 * Private members
	 */
	
	// the connectionwrapper abstracts the type of connection the pool is using.
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
	
	// Logger
	private static Logger logger = Logger.getLogger(BlockingConnectionPool.class);
	
	/**
	 * initialize a blocking connection pool.
	 * 
	 * @param size The number of connections in the pool
	 * @param connectionWrapper A wrapper for the connection type of this pool
	 * @throws SQLException
	 */
	public BlockingConnectionPool(Integer size, ConnectionWrapper connectionWrapper) throws SQLException {
		this.logger.trace("BlockingConstructor Constructor");
		this.poolSize = size;
		this.connectionWrapper = connectionWrapper;
		this.availableConnections = new ArrayList<Connection>( );
		this.usedConnections = new ArrayList<Connection>( );
		this.arrayListLock = new ReentrantLock();
		this.arrayListSemaphore = new Semaphore(size, true);
		
		this.initConnectionPool();
	}
	
	
	/**
	 * This will return an available connection or block until a connection is available.
	 * <p>
	 * If a connection is not available, this call will block
	 * 
	 * @throws SQLException
	 */
	@Override
	public Connection getConnection() throws SQLException {
		this.logger.trace("getConnection");
		
		try {
			this.arrayListSemaphore.acquire();
		}
		catch(InterruptedException ex) {
			this.logger.error("Exception acquiring the semaphor");
			throw new SQLException(ex);
		}
		
		
		Connection con = getAvailableConnection( );
		return con;

	}
	
	
	/**
	 * adds the connection back to the queue of available connections
	 * 
	 * @throws SQLException
	 */
	@Override
	public void releaseConnection(Connection connection) throws SQLException {

		this.logger.trace("releaseConnection");
		
		if(connection == null) { 
			throw new SQLException("Cannot release a null connection");
		}

		try {
			this.arrayListLock.lock();

			// ensure the connection exists in our usedConnections
			if(!this.usedConnections.remove(connection)) {
				throw new SQLException("This connection is not part of this ConnectionPool");
			}

			this.availableConnections.add(connection);

		}
		finally {
			this.arrayListLock.unlock();
		}

		this.arrayListSemaphore.release();
	
	}
	
	/**
	 * Initializes the connection pool with the proper number of connections.
	 * 
	 * @throws SQLException
	 */
	private void initConnectionPool() throws SQLException {
		this.logger.trace("initConnectionPool");
		for(Integer counter = 0; counter < this.poolSize; ++counter) {
			this.availableConnections.add(createConnection());

		}
	}

	/**
	 * 
	 * @return a newly created connection
	 * @throws SQLException
	 */
	private Connection createConnection() throws SQLException {
		this.logger.trace("createConnection");
		return this.connectionWrapper.getConnection();
	}

	
	/**
	 * gets a connection off the available connection array and moves it onto the 
	 * usedConnection array.
	 * 
	 * @return a connection to be used
	 * @throws SQLException
	 */
	private Connection moveConnectionToUsedArray() throws SQLException {
		
		this.logger.trace("moveConnectionToUsedArray");
		
		if(this.availableConnections.size() == 0) {
			this.logger.fatal("There should always be an available connection here.  throwing exception");
			throw new SQLException("There are no connections in the available connections array.  this should never happen.");
		}
			
		// remove the top element and add it to the used connections
		Connection connection = this.availableConnections.remove(0);

		//if the connection is closed
		//create a new connection
		if(connection.isClosed()) {
			connection = createConnection();
		}

		this.usedConnections.add(connection);

		return connection;
	}

	/**
	 * gets the number of available connections
	 * 
	 * @return the number of available connections
	 */
	public Integer getAvailableConnections() {
		this.logger.trace("getAvailableConnections");
		return this.availableConnections.size();
	}
	
	
	/**
	 * gets the number of used connections
	 * 
	 * @return the number of used connections
	 */
	public Integer getUsedConnections() {
		this.logger.trace("getAvailableConnections");
		return this.usedConnections.size();
	}
	
	/**
	 * gets a connection in a non blocking way. if a connection does not become available in the give time
	 * the method will throw
	 * 
	 * @param timeout the maximum time to wait for a permit
	 * @param unit the time unit of the timeout argument
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(long timeout, TimeUnit unit) throws SQLException {
		this.logger.trace("getConnection with timout");

		try {
			boolean ableToAcquire = this.arrayListSemaphore.tryAcquire(timeout, unit);
			if(!ableToAcquire) {
				throw new SQLException("No connections became available");
			}
		}
		catch(InterruptedException ex) {
			this.logger.error("Exception acquiring the semaphor");
			throw new SQLException(ex);
		}
		
		Connection con = getAvailableConnection( );
		return con;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection getAvailableConnection() throws SQLException {
		this.logger.trace("getAvailableConnection");
		Connection con;
		
		try {
			// this will block waiting for a connection
			this.arrayListLock.lock( );
			
			if(this.availableConnections.size() == 0) {
				//this should never happen
				this.logger.fatal("There should always be a connection available here.  A deadlock scenario.");
			}
			
			// there should always be at least 1 available connection at this point.
			con = moveConnectionToUsedArray();
		}
		finally {
			this.arrayListLock.unlock();
		}

		return con;
	}


	
}
