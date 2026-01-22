package com.internship.rblp.config;

import io.ebean.Database;
import io.ebean.config.DatabaseConfig;
import io.ebean.DatabaseFactory;
import io.ebean.datasource.DataSourceConfig;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppDatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppDatabaseConfig.class);

    // initialize DB connection using application.properties
    public static void init(JsonObject config) {
        logger.info("Initializing Database Connection...");

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUsername(config.getString("datasource.username"));
        dataSourceConfig.setPassword(config.getString("datasource.password"));
        dataSourceConfig.setUrl(config.getString("datasource.url"));
        dataSourceConfig.setDriver(config.getString("datasource.driver"));

        DatabaseConfig dbConfig = new DatabaseConfig();

        dbConfig.setDataSourceConfig(dataSourceConfig);
        dbConfig.addPackage("com.internship.rblp.models.entities");

        // Development settings: Generate/Run DDL based on properties
        // This will auto-create your tables based on your Entities
        dbConfig.setDdlGenerate(Boolean.parseBoolean(config.getString("ebean.ddl.generate", "false")));
        dbConfig.setDdlRun(Boolean.parseBoolean(config.getString("ebean.ddl.run", "false")));

        // Explicitly set as default server to allow static access (e.g., User.find.byId(...))
        dbConfig.setDefaultServer(true);

        try {
            Database database = DatabaseFactory.create(dbConfig);
            logger.info("Database initialized successfully: {}", database.name());
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}