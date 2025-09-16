package com.barebonebatch.common.config;

import com.barebonebatch.common.domain.ImportLine;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    @StepScope
    public ItemReader<ImportLine> databaseReader(SqlSessionFactory sqlSessionFactory, @Value("#{jobParameters['wstIden']}") Long wstIden) {
        return new MyBatisPagingItemReaderBuilder<ImportLine>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.barebonebatch.common.dao.JobDao.getLinesByWstIden")
                .parameterValues(Map.of("wstIden", wstIden))
                .pageSize(100)
                .build();
    }
}
