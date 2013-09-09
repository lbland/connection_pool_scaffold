package com.opower.connectionpool;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.opower.connectionpool.BlockingConnectionPool;

/**
 * UT Tets for the BlockingConnectionPool
 * 
 * @author loren_bland
 * @version 1.0
 */
public class TestBlockingConnectionPool {


	private ConnectionWrapper mockConnectionWrapper;
	private Connection mockConnection;
	private BlockingConnectionPool pool;

	
	public void setupSuccess(Integer count) throws SQLException {
		
		for(Integer i = 0; i < count; ++i) {
			expect(mockConnectionWrapper.getConnection( )).andReturn(mockConnection);
			expect(mockConnection.isClosed( )).andReturn(new Boolean(false));
		}
		replay(mockConnectionWrapper);
		replay(mockConnection);
	}
	
	@Before
	public void mockDBConnection( ) throws SQLException {
		
		mockConnectionWrapper = createNiceMock(ConnectionWrapper.class);
		mockConnection = createNiceMock(Connection.class);
	}
	
	
	@Test
	public void simpleConnectionPoolTets() throws SQLException {
			
		setupSuccess(1);

		pool = new BlockingConnectionPool(1, mockConnectionWrapper);

		Connection con = pool.getConnection( );

		assertEquals(pool.getUsedConnections(), new Integer(1));
		assertEquals(pool.getAvailableConnections(), new Integer(0));
		
		verify(mockConnectionWrapper);
	}
	
	@Test
	public void createConnectionPool() throws SQLException {
		setupSuccess(5);

		pool = new BlockingConnectionPool(5, mockConnectionWrapper);

		assertEquals(pool.getAvailableConnections( ), new Integer(5));

		Connection con = pool.getConnection( );

		assertEquals(pool.getAvailableConnections( ), new Integer(4));

		Connection con1 = pool.getConnection( );
		Connection con2 = pool.getConnection( );
		Connection con3 = pool.getConnection( );
		Connection con4 = pool.getConnection( );

		assertEquals(pool.getAvailableConnections( ), new Integer(0));

		pool.releaseConnection(con);
		assertEquals(pool.getAvailableConnections( ), new Integer(1));

		pool.releaseConnection(con1);
		pool.releaseConnection(con2);
		pool.releaseConnection(con3);
		pool.releaseConnection(con4);

		assertEquals(pool.getAvailableConnections( ), new Integer(5));

		verify(mockConnection);
		verify(mockConnectionWrapper);

	}


	/**
	*  DelayReleaseConnection
	*
	*		- This class takes a connection and releases it after sleeping for a small bit of time.
	*		This allows the calling thread to request a connection and be blocked until this class releases the connection.
	*/

	public static class DelayReleaseConnection implements Runnable{
		
		private BlockingConnectionPool pool;
		private Connection connection;

		DelayReleaseConnection(BlockingConnectionPool pool, Connection connection) {
			this.pool = pool;
			this.connection = connection;
		}

		public void run() {
			try {
				
				// 3 second sleep
				Thread.sleep(3000);

			}
			catch(InterruptedException ex) {
				//no op
			}
			finally {
				try {

					this.pool.releaseConnection(this.connection);
				}
				catch(SQLException ex){
					//no op
				}
			}
 		}
	}

	
	@Test
	public void testBlockingRequest() throws SQLException {


		expect(mockConnectionWrapper.getConnection( )).andReturn(mockConnection);
		expect(mockConnection.isClosed( )).andReturn(new Boolean(false));
		expect(mockConnection.isClosed( )).andReturn(new Boolean(false));
		
		replay(mockConnectionWrapper);
		replay(mockConnection);

		pool = new BlockingConnectionPool(1, mockConnectionWrapper);

		assertEquals(pool.getAvailableConnections( ), new Integer(1));

		Connection connection = pool.getConnection( );
		assertEquals(pool.getAvailableConnections( ), new Integer(0));

		Thread delayReleaseThread = new Thread(new DelayReleaseConnection(pool, connection));
		delayReleaseThread.start( );

		Connection connection2 = pool.getConnection( );
		assertEquals(pool.getAvailableConnections( ), new Integer(0));

		pool.releaseConnection(connection2);
		assertEquals(pool.getAvailableConnections( ), new Integer(1));

		verify(mockConnection);
		verify(mockConnectionWrapper);

	}
	
	
	@Test
	public void testReleaseUnknowConnection() throws SQLException {
		setupSuccess(1);
		pool = new BlockingConnectionPool(1, mockConnectionWrapper);
		
		Connection connection = pool.getConnection();
		
		try {
			pool.releaseConnection(null);
		} catch(Exception e) {
			assertEquals(e.getMessage(), "Cannot release a null connection");
		}
		
		Connection badConnection = createMock(Connection.class);
		replay(badConnection);
		try {
			pool.releaseConnection(badConnection);
		} catch(Exception e) {
			assertEquals(e.getMessage(), "This connection is not part of this ConnectionPool");
		}
		
		pool.releaseConnection(connection);

		verify(mockConnection);
		verify(mockConnectionWrapper);

	}
	
}
