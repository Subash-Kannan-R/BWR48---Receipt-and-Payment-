package com.kfurban.dhanam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * DataSourceAutoConfiguration is excluded because this app manages multiple
 * datasources itself (N location schemas + 1 results schema) via
 * DhanamDataSources, rather than a single spring.datasource.* connection.
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DhanamApplication {
    public static void main(String[] args) {
        SpringApplication.run(DhanamApplication.class, args);
    }
}
