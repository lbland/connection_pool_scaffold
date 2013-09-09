package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A Connection wrapper.  This class is used to abstract out the Connection so mocking
 * is easier.
 * 
 * @author loren_bland
 * @version 1.0
 */

public interface ConnectionWrapper {

	/**
	 * 
	 * 
	 * @return
	 * @throws SQLException
	 */
	Connection getConnection( ) throws SQLException;
	
}
