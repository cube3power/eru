/******************************************************************************
 * Copyright (c) 2017 Assemblits contributors                                 *
 *                                                                            *
 * This file is part of Eru The open JavaFX SCADA by Assemblits Organization. *
 *                                                                            *
 * Eru The open JavaFX SCADA is free software: you can redistribute it        *
 * and/or modify it under the terms of the GNU General Public License         *
 *  as published by the Free Software Foundation, either version 3            *
 *  of the License, or (at your option) any later version.                    *
 *                                                                            *
 * Eru is distributed in the hope that it will be useful,                     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.            *
 ******************************************************************************/
package org.assemblits.eru.persistence;

import lombok.extern.slf4j.Slf4j;
import org.assemblits.eru.preferences.EruPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("org.assemblits.eru")
@EntityScan("org.assemblits.eru")
public class DataSourceConfig {

    @Autowired
    private EruPreferences eruPreferences;

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource dataSource;
        try {
            log.info("Building main database connection.");
            dataSource = getMainDataSource();
            dataSource.getConnection().isValid(500);
        } catch (Exception e) {
            log.error("Main database not valid: {}", e.getMessage());
            log.info("Building backup database connection.");
            dataSource =getBackupDataSource();
        }
        return dataSource;
    }

    private DataSource getMainDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.jdbc.Driver")
                .username(eruPreferences.getEruDatabaseUsername().get())
                .password(eruPreferences.getEruDatabasePassword().get())
                .url(eruPreferences.getEruDatabaseURL().get())
                .build();
    }

    private DataSource getBackupDataSource() {
        final String backupriver = "org.h2.Driver";
        final String backupUsername = "eru";
        final String backupPassword = "951753";
        final String backupUrl = "jdbc:h2:" + eruPreferences.getApplicationDirectory().get() + "/eru;DB_CLOSE_ON_EXIT=FALSE";
        return DataSourceBuilder.create()
                .driverClassName(backupriver)
                .username(backupUsername)
                .password(backupPassword)
                .url(backupUrl)
                .build();
    }
}
