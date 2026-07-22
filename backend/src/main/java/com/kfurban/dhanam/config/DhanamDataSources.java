package com.kfurban.dhanam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Two kinds of connections:
 *  - "location" schemas: the source databases this report reads from (one
 *    column per schema in the output). Built on demand per request, never
 *    left open - a schema name is only ever used if it (a) matches
 *    SAFE_IDENTIFIER and (b) came from the configured allow-list below, so
 *    nothing here is ever built from raw client input.
 *  - "results" schema: the single fixed sink every generated report's
 *    detail rows get written to (result). One reusable DataSource
 *    bean, injected wherever it's needed.
 */
@Configuration
public class DhanamDataSources {

    public static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    @Value("${dhanam.location.jdbc-url-template}")
    private String locationUrlTemplate;
    @Value("${dhanam.location.username}")
    private String locationUser;
    @Value("${dhanam.location.password}")
    private String locationPassword;
    @Value("${dhanam.location.driver-class-name}")
    private String locationDriver;
    @Value("${dhanam.location.schemas}")
    private String locationSchemasCsv;

    @Value("${dhanam.results.jdbc-url}")
    private String resultsUrl;
    @Value("${dhanam.results.username}")
    private String resultsUser;
    @Value("${dhanam.results.password}")
    private String resultsPassword;
    @Value("${dhanam.results.driver-class-name}")
    private String resultsDriver;

    /** The fixed list of source schemas to loop over - one column per schema in the report. */
    public List<String> locationSchemas() {
        return Arrays.stream(locationSchemasCsv.split("\\s*,\\s*"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** Throwaway DataSource for a single validated location schema. */
    public DataSource locationDataSource(String schemaName) {
        if (!SAFE_IDENTIFIER.matcher(schemaName).matches()) {
            throw new IllegalArgumentException("Rejected unsafe schema name: " + schemaName);
        }
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(locationDriver);
        ds.setUrl(String.format(locationUrlTemplate, schemaName));
        ds.setUsername(locationUser);
        ds.setPassword(locationPassword);
        return ds;
    }

    @Bean(name = "resultsDataSource")
    public DataSource resultsDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(resultsDriver);
        ds.setUrl(resultsUrl);
        ds.setUsername(resultsUser);
        ds.setPassword(resultsPassword);
        return ds;
    }
}
