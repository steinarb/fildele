/*
 * Copyright 2025 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.fildele.db.liquibase;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.assertj.db.type.AssertDbConnectionFactory;
import org.junit.jupiter.api.Test;
import org.ops4j.pax.jdbc.derby.impl.DerbyDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

import liquibase.exception.LiquibaseException;

class FildeleLiquibaseTest {
    DataSourceFactory derbyDataSourceFactory = new DerbyDataSourceFactory();

    @Test
    void testCreateSchema() throws Exception {
        var fildeleLiquibase = new FildelepLiquibase();
        var datasource = createDataSource("fildele");
        var assertjConnection = AssertDbConnectionFactory.of(datasource).create();

        fildeleLiquibase.createInitialSchema(datasource.getConnection());

        var accounts1 = assertjConnection.table("fildele_accounts").build();
        assertThat(accounts1).exists().isEmpty();

        addAccounts(datasource);

        var accounts2 = assertjConnection.table("fildele_accounts").build();
        assertThat(accounts2).exists().hasNumberOfRows(1).row().column("username").hasValues("admin");

        var incrementSteps1 = assertjConnection.table("counter_increment_steps").build();
        assertThat(incrementSteps1).exists().isEmpty();

        addCounterIncrementSteps(datasource);

        var incrementSteps2 = assertjConnection.table("counter_increment_steps").build();
        assertThat(incrementSteps2).hasNumberOfRows(1);

        var counters1 = assertjConnection.table("counters").build();
        assertThat(counters1).exists().isEmpty();

        addCounters(datasource);

        var counters2 = assertjConnection.table("counters").build();
        assertThat(counters2).hasNumberOfRows(1);

        try(var connection = datasource.getConnection()) {
            int accountIdNotMatchingAccount = 375;
            assertThrows(SQLException.class,() -> addCounterIncrementStep(connection, accountIdNotMatchingAccount, 10));
            assertThrows(SQLException.class,() -> addCounter(connection, accountIdNotMatchingAccount, 4));
        }

        fildeleLiquibase.updateSchema(datasource.getConnection());
    }

    @Test
    void testCreateSchemaAndFail() throws Exception {
        var datasource = createDataSource("fildele");
        var connection = spy(datasource.getConnection());
        // A Derby JDBC connection wrapped in a Mockito spy() fails om Connection.setAutoClosable()

        var fildeleLiquibase = new FildelepLiquibase();

        var ex = assertThrows(
            LiquibaseException.class,
            () -> fildeleLiquibase.createInitialSchema(connection));
        assertThat(ex.getMessage()).startsWith("java.sql.SQLException: Cannot set Autocommit On when in a nested connection");
    }

    @Test
    void testCreateSchemaAndFailOnConnectionClose() throws Exception {
        var datasource = createDataSource("fildele2");
        var connection = spy(datasource.getConnection());
        doNothing().when(connection).setAutoCommit(anyBoolean());
        doThrow(Exception.class).when(connection).close();

        var fildeleLiquibase = new FildelepLiquibase();

        var ex = assertThrows(
            LiquibaseException.class,
            () -> fildeleLiquibase.createInitialSchema(connection));
        assertThat(ex.getMessage()).startsWith("java.lang.Exception");
    }

    private void addAccounts(DataSource datasource) throws Exception {
        try(var connection = datasource.getConnection()) {
            addAccount(connection, "admin");
        }
    }

    private void addCounterIncrementSteps(DataSource datasource) throws Exception {
        try(var connection = datasource.getConnection()) {
            addCounterIncrementStep(connection, findAccountId(connection, "admin"), 10);
        }
    }

    private void addCounters(DataSource datasource) throws Exception {
        try(var connection = datasource.getConnection()) {
            addCounter(connection, findAccountId(connection, "admin"), 3);
        }
    }

    private int addAccount(Connection connection, String username) throws Exception {
        var sql = "insert into fildele_accounts (username) values (?)";
        try(var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }

        return findAccountId(connection, username);
    }

    private void addCounterIncrementStep(Connection connection, int accountid, int counterIncrementStep) throws Exception {
        var sql = "insert into counter_increment_steps (account_id, counter_increment_step) values (?, ?)";
        try(var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountid);
            statement.setInt(2, counterIncrementStep);
            statement.executeUpdate();
        }
    }

    private void addCounter(Connection connection, int accountid, int count) throws Exception {
        var sql = "insert into counters (account_id, counter) values (?, ?)";
        try(var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountid);
            statement.setInt(2, count);
            statement.executeUpdate();
        }
    }

    private int findAccountId(Connection connection, String username) throws Exception {
        var sql = "select account_id from fildele_accounts where username=?";
        try(var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try(var results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        }

        return -1;
    }

    private DataSource createDataSource(String dbname) throws Exception {
        var properties = new Properties();
        properties.setProperty(DataSourceFactory.JDBC_URL, "jdbc:derby:memory:" + dbname + ";create=true");
        return derbyDataSourceFactory.createDataSource(properties);
    }

}
