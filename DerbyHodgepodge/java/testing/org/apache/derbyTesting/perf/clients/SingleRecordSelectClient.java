/*

Derby - Class org.apache.derbyTesting.perf.clients.SingleRecordSelectClient

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

package org.apache.derbyTesting.perf.clients;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

/**
 * Client which performs single-record lookups on tables generated by
 * {@code SingleRecordFiller}. Each time the client's {@code doWork()} method
 * is called, it will pick one of the tables randomly, and select one random
 * record in that table.
 */
public class SingleRecordSelectClient implements Client {

    private Connection conn;

    private final PreparedStatement[] pss;
    private final Random r;
    private final int tableSize;
    private final int dataType;
    private final boolean secondaryIndex;
    private final boolean noIndex;

    /**
     * Construct a new single-record select client which fetches VARCHAR data
     * by primary key.
     *
     * @param records the number of records in each table in the test
     * @param tables the number of tables in the test
     */
    public SingleRecordSelectClient(int records, int tables) {
        this(records, tables, Types.VARCHAR, false, false);
    }

    /**
     * Construct a new single-record select client.
     *
     * @param records the number of records in each table in the test
     * @param tables the number of tables in the test
     * @param type the data type of the text column
     * ({@code java.sql.Types.VARCHAR}, {@code java.sql.Types.BLOB} or
     * {@code java.sql.Types.CLOB})
     * @param secIndex if {@code true}, select by secondary index column
     * instead of primary key column
     * @param nonIndexed if {@code true}, select by non-indexed column
     * instead of primary key column
     */
    public SingleRecordSelectClient(int records, int tables, int type,
                                    boolean secIndex, boolean nonIndexed) {
        tableSize = records;
        r = new Random();
        pss = new PreparedStatement[tables];
        dataType = type;
        if (secIndex && nonIndexed) {
            throw new IllegalArgumentException(
                "Cannot select on both secondary index and non-index column");
        }
        secondaryIndex = secIndex;
        noIndex = nonIndexed;
    }

    public void init(Connection c) throws SQLException {
        for (int i = 0; i < pss.length; i++) {
            String tableName =
                    SingleRecordFiller.getTableName(tableSize, i, dataType,
                                                    secondaryIndex, noIndex);
            String column = "ID";
            if (secondaryIndex) {
                column = "SEC";
            } else if (noIndex) {
                column = "NI";
            }
            String sql =
                    "SELECT ID, TEXT FROM " + tableName +
                    " WHERE " + column + " = ?";
            pss[i] = c.prepareStatement(sql);
        }
        c.setAutoCommit(false);
        conn = c;
    }

    public void doWork() throws SQLException {
        PreparedStatement ps = pss[r.nextInt(pss.length)];
        ps.setInt(1, r.nextInt(tableSize));
        ResultSet rs = ps.executeQuery();
        rs.next();
        rs.getInt(1);
        fetchTextColumn(rs, 2);
        rs.close();
        conn.commit();
    }

    public void printReport(PrintStream out) {}
    
    /**
     * Make sure the text column is retrieved and read. Different methods
     * are used for the retrieval based on whether the column is a VARCHAR,
     * a BLOB or a CLOB.
     */
    private void fetchTextColumn(ResultSet rs, int column) throws SQLException {
        if (dataType == Types.VARCHAR) {
            rs.getString(column);
        } else if (dataType == Types.CLOB) {
            rs.getClob(column).getSubString(1, SingleRecordFiller.TEXT_SIZE);
        } else if (dataType == Types.BLOB) {
            rs.getBlob(column).getBytes(1, SingleRecordFiller.TEXT_SIZE);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
