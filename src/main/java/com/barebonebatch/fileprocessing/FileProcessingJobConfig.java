package com.mycompany.mybatchapp.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class FileProcessingJobConfig {
/*
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    public FileProcessingJobConfig(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }

    // A job-level listener to perform actions after job completion
    @Bean
    public JobExecutionListener jobCompletionListener() {
        return new JobCompletionNotificationListener();
    }

    // Step-scoped reader to dynamically read the input file passed as a job parameter
    @Bean
    public FlatFileItemReader<UserData> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
        return new FlatFileItemReaderBuilder<UserData>()
                .name("userDataItemReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .names("firstName", "lastName", "email")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(UserData.class);
                }})
                .build();
    }

    // Custom processor to validate and transform each item
    @Bean
    public UserDataItemProcessor processor() {
        return new UserDataItemProcessor();
    }

    // Item writer to save processed items to the database
    @Bean
    public JdbcBatchItemWriter<UserData> writer() {
        return new JdbcBatchItemWriterBuilder<UserData>()
                .itemSqlParameterSourceProvider(new org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO users (first_name, last_name, email) VALUES (:firstName, :lastName, :email)")
                .dataSource(dataSource)
                .build();
    }

    // The single step of the job
    @Bean
    public Step processFileStep(FlatFileItemReader<UserData> reader,
                                UserDataItemProcessor processor,
                                JdbcBatchItemWriter<UserData> writer) {
        return new StepBuilder("processFileStep", jobRepository)
                .<UserData, UserData>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // The main job definition
    @Bean
    public Job fileProcessingJob(Step processFileStep, JobExecutionListener listener) {
        return new JobBuilder("fileProcessingJob", jobRepository)
                .listener(listener)
                .start(processFileStep)
                .build();
    }*/
}