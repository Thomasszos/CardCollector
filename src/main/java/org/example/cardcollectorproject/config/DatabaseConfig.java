package org.example.cardcollectorproject.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();
    private static DatabaseConfig instance;

    private DatabaseConfig() {
        loadProperties();
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading application.properties", ex);
        }
    }

    public String getCosmosEndpoint() {
        return properties.getProperty("azure.cosmos.endpoint");
    }

    public String getCosmosKey() {
        return properties.getProperty("azure.cosmos.key");
    }

    public String getCosmosDatabaseName() {
        return properties.getProperty("azure.cosmos.database");
    }

    public String getCosmosContainerName() {
        return properties.getProperty("azure.cosmos.container");
    }
}
