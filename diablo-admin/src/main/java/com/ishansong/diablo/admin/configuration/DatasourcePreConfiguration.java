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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@MapperScan(basePackages = "com.ishansong.diablo.admin.pre.mapper", sqlSessionTemplateRef = "preSqlSessionTemplate")
public class DatasourcePreConfiguration {

    @Bean(name = "preDatasource")
    @ConfigurationProperties(prefix = "diablo.gateway.pre")
    public DataSource preDatasource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "preSqlSessionFactory")
    public SqlSessionFactory preSqlSessionFactory(@Qualifier("preDatasource") DataSource dataSource) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:preMappers/*.xml"));
        sqlSessionFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:/mybatis/mybatis-config.xml"));

        try {
            String url = dataSource.getConnection().getMetaData().getURL();

            log.info("DatasourcePreConfiguration preDatasource connection url={}", url);
        } catch (Exception e) {
            log.error("DatasourcePreConfiguration preDatasource failed, cause:{}", Throwables.getStackTraceAsString(e));
        }

        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "preTransactionManager")
    public DataSourceTransactionManager preTransactionManager(@Qualifier("preDatasource") DataSource dataSource) {

        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "preSqlSessionTemplate")
    public SqlSessionTemplate preSqlSessionTemplate(@Qualifier("preSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {

        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
