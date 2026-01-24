package com.internship.rblp.config;

import io.ebean.Database;
import io.ebean.config.DatabaseConfig;
import io.ebean.DatabaseFactory;
import io.ebean.datasource.DataSourceConfig;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppDatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppDatabaseConfig.class);

    // initialize DB connection using application.properties
    public static void init(JsonObject config) {
        logger.info("Initializing Database Connection...");
        Dotenv dotenv = Dotenv.load();

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUsername(dotenv.get("DB_USERNAME"));
        dataSourceConfig.setPassword(dotenv.get("DB_PASSWORD"));
        dataSourceConfig.setUrl(dotenv.get("DB_URL"));
        dataSourceConfig.setDriver(dotenv.get("DB_DRIVER"));

        DatabaseConfig dbConfig = new DatabaseConfig();

        dbConfig.setDataSourceConfig(dataSourceConfig);
        dbConfig.addPackage("com.internship.rblp.models.entities");

        dbConfig.setDdlGenerate(Boolean.parseBoolean(dotenv.get("EBEAN_DDL_GENERATE", "false")));
        dbConfig.setDdlRun(Boolean.parseBoolean(dotenv.get("EBEAN_DDL_RUN", "false")));

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