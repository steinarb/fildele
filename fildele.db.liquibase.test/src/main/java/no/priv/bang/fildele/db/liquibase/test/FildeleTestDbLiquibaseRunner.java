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
package no.priv.bang.fildele.db.liquibase.test;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import liquibase.exception.LiquibaseException;
import no.priv.bang.fildele.db.liquibase.FildelepLiquibase;

@Component(immediate=true, property = "name=fildeledb")
public class FildeleTestDbLiquibaseRunner implements PreHook {

    @Activate
    public void activate() {
        // Called after all injections have been satisfied and before the PreHook service is exposed
    }

    @Override
    public void prepare(DataSource datasource) throws SQLException {
        var fildeleLiquibase = new FildelepLiquibase();
        try (var connect = datasource.getConnection()) {
            fildeleLiquibase.createInitialSchema(connect);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Error creating fildele test database schema", e);
        }

        try (var connect = datasource.getConnection()) {
            insertMockData(connect, fildeleLiquibase);
        } catch (Exception e) {
            throw new SQLException("Error inserting fildele test database mock data", e);
        }

        try (var connect = datasource.getConnection()) {
            fildeleLiquibase.updateSchema(connect);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Error updating fildele test database schema", e);
        }
    }

    public void insertMockData(Connection connect, FildelepLiquibase fildeleLiquibase) throws LiquibaseException {
        fildeleLiquibase.applyLiquibaseChangelist(connect, "sql/data/db-changelog.xml", getClass().getClassLoader());
    }

}
