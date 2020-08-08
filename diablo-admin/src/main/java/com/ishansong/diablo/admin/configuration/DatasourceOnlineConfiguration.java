package com.ishansong.diablo.admin.configuration;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@MapperScan(basePackages = "com.ishansong.diablo.admin.mapper", sqlSessionTemplateRef = "onlineSqlSessionTemplate")
public class DatasourceOnlineConfiguration {

    @Bean(name = "onlineDatasource")
    @ConfigurationProperties(prefix = "diablo.admin.db.online")
    @Primary
    public DataSource onlineDataSource() {

        return DataSourceBuilder.create().build();
    }

    @Bean(name = "onlineSqlSessionFactory")
    @Primary
    public SqlSessionFactory onlineSqlSessionFactory(@Qualifier("onlineDatasource") DataSource dataSource) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mappers/*.xml"));
        sqlSessionFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:/mybatis/mybatis-config.xml"));

        try {
            String url = dataSource.getConnection().getMetaData().getURL();

            log.info("DatasourceOnlineConfiguration onlineDataSource connection url={}", url);
        } catch (Exception e) {
            log.error("DatasourceOnlineConfiguration onlineDataSource failed, cause:{}", Throwables.getStackTraceAsString(e));
        }

        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "onlineTransactionManager")
    @Primary
    public DataSourceTransactionManager onlineTransactionManager(@Qualifier("onlineDatasource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "onlineSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate onlineSqlSessionTemplate(@Qualifier("onlineSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {

        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
