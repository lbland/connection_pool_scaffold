
package com.opower.connectionpool;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.SQLException;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

/**
 * UT for the ConnectionWrapper
 * 
 * @author loren_bland
 *
 */
public class TestConnectionWrapper {

	
	private ConnectionWrapper connectionWrapper;
	private DataSource mockDataSource;
	private Connection mockConnection;

	
	@Before
	public void mockConnection() throws SQLException {
		
		mockDataSource = createNiceMock(DataSource.class);
		mockConnection = createNiceMock(Connection.class);
	}
	
	@Test
	public void testConnection() throws SQLException {

		expect(mockDataSource.getConnection()).andReturn(mockConnection);
		replay(mockDataSource);
		replay(mockConnection);
		
		connectionWrapper = new ConnectionWrapperImplementation(mockDataSource);
		
		Connection con = connectionWrapper.getConnection();
		
	}
}
