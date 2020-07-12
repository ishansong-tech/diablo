package com.ishansong.diablo.admin.service.init;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;

//@Component
public class LocalDataSourceLoader implements InstantiationAwareBeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDataSourceLoader.class);

    private static final String SCHEMA_SQL_FILE = "META-INF/schema.sql";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties) {
            this.init((DataSourceProperties) bean);
        }
        return bean;
    }

    protected void init(DataSourceProperties properties) {

        String jdbcUrl = properties.getUrl();
        jdbcUrl = StringUtils.replace(jdbcUrl, "/soul?", "?");

        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, properties.getUsername(), properties.getPassword());
            this.execute(connection);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }

    }

    private void execute(Connection conn) throws Exception {

        ScriptRunner runner = new ScriptRunner(conn);
        // doesn't print logger
        runner.setLogWriter(null);

        Resources.setCharset(Charset.forName("UTF-8"));
        Reader read = Resources.getResourceAsReader(SCHEMA_SQL_FILE);
        LOGGER.info("execute soul schema sql: {}", SCHEMA_SQL_FILE);
        runner.runScript(read);

        runner.closeConnection();
        conn.close();

    }

}
