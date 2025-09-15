package com.barebonebatch.dbprocessing;

import com.barebonebatch.common.config.JobIdentifier;
import com.barebonebatch.common.domain.ImportLine;
import com.barebonebatch.common.listener.JobCompletionNotificationListener;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;


@Configuration
public class DatabaseProcessingJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;


    // Step-scoped reader to dynamically read the input file passed as a job parameter
//    @Bean
//    public FlatFileItemReader<UserData> reader(@Value("#{jobParameters['wstIden']}") Long wstIden) {
//        return new FlatFileItemReaderBuilder<UserData>()
//                .name("userDataItemReader")
//                .resource(new FileSystemResource(inputFile))
//                .delimited()
//                .names("firstName", "lastName", "email")
//                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
//                    setTargetType(UserData.class);
//                }})
//                .build();
//    }


    // Custom processor to validate and transform each item
//    @Bean
//    public UserDataItemProcessor processor() {
//        return new UserDataItemProcessor();
//    }

    // Item writer to save processed items to the database
//    @Bean
//    public MyBatisBatchItemWriter<Dummy> writer() {
//        return new JdbcBatchItemWriterBuilder<UserData>()
//                .itemSqlParameterSourceProvider(new org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider<>())
//                .sql("INSERT INTO users (first_name, last_name, email) VALUES (:firstName, :lastName, :email)")
//                .dataSource(dataSource)
//                .build();
//    }

    @Bean
    @StepScope
    public MyBatisPagingItemReader<ImportLine> databaseReader(SqlSessionFactory sqlSessionFactory, @Value("#{jobParameters['wstIden']}") Long wstIden) {
        return new MyBatisPagingItemReaderBuilder<ImportLine>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.barebonebatch.common.dao.JobDao.getLinesByWstIden")
                .parameterValues(Map.of("wstIden", wstIden))
                .pageSize(100) // TODO Adjust as needed
                .saveState(true) // TODO I'm not sure about this
                .build();
    }

    // The single step of the job
    @Bean
    public Step processDbStep(MyBatisPagingItemReader<ImportLine> databaseReader
//                                ,UserDataItemProcessor processor
//                                ,JdbcBatchItemWriter<UserData> writer
    ) {
        return new StepBuilder("processFileStep", jobRepository).chunk(100, transactionManager)
                .reader(databaseReader)
//                .processor(processor)
                .writer(chunk -> {})
                .build();
    }

    // The main job definition
    @Bean
    @JobIdentifier(value = "", description = "", uatIdf = "", prodIdf = "")
    public Job fileProcessingJob(Step processDbStep,
                                 JobCompletionNotificationListener jobCompletionNotificationListener) {
        return new JobBuilder("dataBaseProcessingJob", jobRepository)
                .listener(jobCompletionNotificationListener)
                .start(processDbStep)
                .build();
    }
}
