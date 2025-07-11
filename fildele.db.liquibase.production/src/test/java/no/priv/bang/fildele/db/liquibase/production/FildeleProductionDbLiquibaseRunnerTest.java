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
package no.priv.bang.fildele.db.liquibase.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.assertj.db.type.AssertDbConnectionFactory;
import org.junit.jupiter.api.Test;
import org.ops4j.pax.jdbc.derby.impl.DerbyDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

class FildeleProductionDbLiquibaseRunnerTest {

    @Test
    void testCreateAndVerifySomeDataInSomeTables() throws Exception {
        var dataSourceFactory = new DerbyDataSourceFactory();
        var properties = new Properties();
        properties.setProperty(DataSourceFactory.JDBC_URL, "jdbc:derby:memory:fildele;create=true");
        var datasource = dataSourceFactory.createDataSource(properties);
        var assertjConnection = AssertDbConnectionFactory.of(datasource).create();

        var runner = new FildeleProductionDbLiquibaseRunner();
        runner.activate();
        runner.prepare(datasource);
        var accounts1 = assertjConnection.table("fildele_accounts").build();
        assertThat(accounts1).exists().isEmpty();
        addAccount(datasource, "jd");
        var accounts2 = assertjConnection.table("fildele_accounts").build();
        assertThat(accounts2).exists().hasNumberOfRows(1).row().column("username").hasValues("jd");
    }


    @Test
    void testFailInGettingConnectionWhenCreatingInitialSchema() throws Exception {
        var datasource = mock(DataSource.class);
        when(datasource.getConnection()).thenThrow(new SQLException("Failed to get connection"));

        var runner = new FildeleProductionDbLiquibaseRunner();
        runner.activate();
        var e = assertThrows(
            SQLException.class,
            () -> runner.prepare(datasource));
        assertThat(e.getMessage()).startsWith("Failed to get connection");
    }


    @Test
    void testFailWhenCreatingInitialSchema() throws Exception {
        var connection = spy(createDataSource("fildele1").getConnection());
        var datasource = mock(DataSource.class);
        when(datasource.getConnection()).thenReturn(connection);

        var runner = new FildeleProductionDbLiquibaseRunner();
        runner.activate();
        var e = assertThrows(
            SQLException.class,
            () -> runner.prepare(datasource));
        assertThat(e.getMessage()).startsWith("Failed to create schema in fildele PostgreSQL database");
    }

    @Test
    void testFailWhenAddingMockData() throws Exception {
        var connection = spy(createDataSource("fildele1").getConnection());
        var datasource = spy(createDataSource("fildele2"));
        when(datasource.getConnection())
            .thenCallRealMethod()
            .thenReturn(connection);

        var runner = new FildeleProductionDbLiquibaseRunner();
        runner.activate();
        var e = assertThrows(
            SQLException.class,
            () -> runner.prepare(datasource));
        assertThat(e.getMessage()).startsWith("Failed to insert initial data into fildele PostgreSQL database");
    }


    @Test
    void testFailWhenGettingConnectionForUpdatingSchema() throws Exception {
        var datasource = spy(createDataSource("fildele3"));
        when(datasource.getConnection())
            .thenCallRealMethod()
            .thenCallRealMethod()
            .thenThrow(new SQLException("Failed to get connection"));

        var runner = new FildeleProductionDbLiquibaseRunner();
        runner.activate();
        var e = assertThrows(
            SQLException.class,
            () -> runner.prepare(datasource));
        assertThat(e.getMessage()).startsWith("Failed to get connection");
    }


    @Test
    void testFailWhenUpdatingSchema() throws Exception {
        var connection = spy(createDataSource("fildele4").getConnection());
        var datasource = spy(createDataSource("fildele4"));
        when(datasource.getConnection())
            .thenCallRealMethod()
            .thenCallRealMethod()
            .thenReturn(connection);

        var runner = new FildeleProductionDbLiquibaseRunner();
        runner.activate();
        var e = assertThrows(
            SQLException.class,
            () -> runner.prepare(datasource));
        assertThat(e.getMessage()).startsWith("Failed to modify schema of fildele PostgreSQL database");
    }


    private int addAccount(DataSource datasource, String username) throws Exception {
        try (var connection = datasource.getConnection()) {
            try(var statement = connection.prepareStatement("insert into fildele_accounts (username) values (?)")) {
                statement.setString(1, username);
                statement.executeUpdate();
            }
        }
        int accountId = -1;
        try (var connection = datasource.getConnection()) {
            try(var statement = connection.prepareStatement("select * from fildele_accounts where username=?")) {
                statement.setString(1, username);
                try (var results = statement.executeQuery()) {
                    results.next();
                    accountId = results.getInt(1);
                }
            }
        }
        return accountId;
    }


    private DataSource createDataSource(String dbname) throws SQLException {
        var dataSourceFactory = new DerbyDataSourceFactory();
        var properties = new Properties();
        properties.setProperty(DataSourceFactory.JDBC_URL, "jdbc:derby:memory:" + dbname + ";create=true");
        return dataSourceFactory.createDataSource(properties);
    }

}
