/*

   Derby - Class org.apache.derby.jdbc.Driver30

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import org.apache.derby.iapi.jdbc.BrokeredConnection;
import org.apache.derby.iapi.jdbc.BrokeredConnectionControl;
import org.apache.derby.iapi.jdbc.ResourceAdapter;
import org.apache.derby.impl.jdbc.*;

/**
	This class extends the local20 JDBC driver in order to determine at JBMS
	boot-up if the JVM that runs us does support JDBC 3.0. If it is the case
	then we will load the appropriate class(es) that have JDBC 3.0 new public
	methods and sql types.
 
*/

public class Driver30 extends Driver20 {

	/**
 	 * Get a new nested connection.
	 *
	 * @param conn	The EmbedConnection.
	 *
	 * @return A nested connection object.
	 *
	 */
	public Connection getNewNestedConnection(EmbedConnection conn)
	{
        return new EmbedConnection(conn);
	}

	/*
		Methods to be overloaded in sub-implementations such as
		a tracing driver.
  */
	protected EmbedConnection getNewEmbedConnection(String url, Properties info)
		 throws SQLException 
	{
        return new EmbedConnection(this, url, info);
	}

	/**
	 	@exception SQLException if fails to create statement
	 */
	public java.sql.PreparedStatement
	newEmbedPreparedStatement (
			EmbedConnection conn,
			String stmt,
			boolean forMetaData,
			int resultSetType,
			int resultSetConcurrency,
			int resultSetHoldability,
			int autoGeneratedKeys,
			int[] columnIndexes,
			String[] columnNames)
    throws SQLException
	{
		return new EmbedPreparedStatement30(conn,
								stmt,
								forMetaData,
								resultSetType,
								resultSetConcurrency,
								resultSetHoldability,
								autoGeneratedKeys,
								columnIndexes,
								columnNames);
	}

	/**
	 	@exception SQLException if fails to create statement
	 */
	public java.sql.CallableStatement
	newEmbedCallableStatement(
			EmbedConnection conn,
			String stmt,
			int resultSetType,
			int resultSetConcurrency,
			int resultSetHoldability)
    throws SQLException
	{
		return new EmbedCallableStatement30(conn,
								stmt,
								resultSetType,
								resultSetConcurrency,
								resultSetHoldability);
	}
	public BrokeredConnection newBrokeredConnection(
            BrokeredConnectionControl control)
        throws SQLException
    {
        return new BrokeredConnection(control);
	}

    /**
     * Create and return an EmbedPooledConnection from the received instance
     * of EmbeddedDataSource.
     */
    protected PooledConnection getNewPooledConnection(
        EmbeddedBaseDataSource eds, String user, String password,
        boolean requestPassword) throws SQLException
    {
        return new EmbedPooledConnection(
            eds, user, password, requestPassword);
    }

    /**
     * Create and return an EmbedXAConnection from the received instance
     * of EmbeddedDataSource.
     */
    protected XAConnection getNewXAConnection(
        EmbeddedBaseDataSource eds, ResourceAdapter ra,
        String user, String password, boolean requestPassword)
        throws SQLException
    {
        return new EmbedXAConnection(
            eds, ra, user, password, requestPassword);
    }

}
