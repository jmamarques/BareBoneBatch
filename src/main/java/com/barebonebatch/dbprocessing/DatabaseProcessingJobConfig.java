package com.barebonebatch.dbprocessing;

import com.barebonebatch.common.config.JobIdentifier;
import com.barebonebatch.common.dao.JobDao;
import com.barebonebatch.common.domain.ImportLine;
import com.barebonebatch.common.domain.Mapping;
import com.barebonebatch.common.listener.ChunkErrorListener;
import com.barebonebatch.common.listener.JobCompletionNotificationListener;
import com.barebonebatch.common.processor.ImportLineProcessor;
import com.barebonebatch.common.reader.SkippedItemsReader;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

/**
 * Spring Batch configuration for the database processing job.
 * This class defines the beans required to read from a database, process the data,
 * and write the results back, orchestrated by a Spring Batch Job.
 */
@Configuration
public class DatabaseProcessingJobConfig {

    @Autowired
    private JobDao jobDao;
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * Configures the item writer for persisting {@link Dummy} objects to the database.
     * This writer uses MyBatis in batch mode for efficient database insertion.
     *
     * @return A {@link MyBatisBatchItemWriter} configured to insert {@link Dummy} objects.
     */
    @Bean
    public ItemWriter<Dummy> dummyWriter(SqlSessionFactory sqlSessionFactory) {
        MyBatisBatchItemWriter<Dummy> writer = new MyBatisBatchItemWriter<>();
        writer.setStatementId("com.barebonebatch.common.dao.JobDao.dummyinsert");
        writer.setSqlSessionFactory(sqlSessionFactory);
        return writer;
    }

    /**
     * Configures the item processor for transforming {@link ImportLine} objects into {@link Dummy} objects.
     * This processor is lazy-initialized and uses a predefined mapping configuration ('mapping_1')
     * fetched from the database to map fixed-length string data to the target object.
     *
     * @return An {@link ItemProcessor} capable of converting an {@link ImportLine} to a {@link Dummy} object.
     * @throws RuntimeException if the required mapping configuration is not found.
     */
    @Bean
    @Lazy
    public ItemProcessor<ImportLine, Dummy> dummyProcessor() {
        String mappingId = "mapping_1";
        Mapping mapping = jobDao.getMappingById(mappingId);
        if (mapping == null || mapping.getFields() == null) {
            throw new RuntimeException("Mapping not found: " + mappingId);
        }
        return new ImportLineProcessor<>(Dummy.class, mapping.getFields());
    }

    /**
     * Defines the main processing step for the batch job.
     * This step reads {@link ImportLine} items, processes them into {@link Dummy} objects,
     * and writes them to the database in chunks of 100.
     * <p>
     * This step is fault-tolerant. If an exception occurs during the processing of an item,
     * the step will skip the problematic item and continue with the next one, up to a limit of 10 skips.
     * The provided {@link ChunkErrorListener} will collect all skipped items.
     *
     * @param databaseReader     The {@link ItemReader} that provides {@link ImportLine} data.
     * @param dummyProcessor     The {@link ItemProcessor} that transforms the data.
     * @param dummyWriter        The {@link ItemWriter} that persists the transformed {@link Dummy} objects.
     * @param chunkErrorListener The listener to capture skipped items.
     * @return A configured {@link Step} for the job.
     */
    @Bean
    public Step processDbStep(ItemReader<ImportLine> databaseReader,
                              ItemProcessor<ImportLine, Dummy> dummyProcessor,
                              ItemWriter<Dummy> dummyWriter,
                              ChunkErrorListener chunkErrorListener,
                              ExecutionContextPromotionListener promotionListener) {
        return new StepBuilder("processDbStep", jobRepository)
                .<ImportLine, Dummy>chunk(100, transactionManager)
                .reader(databaseReader)
                .processor(dummyProcessor)
                .writer(dummyWriter)
                .faultTolerant()
                .skip(Exception.class) // Skip any exception thrown during processing
                .skipLimit(10) // Fail the step after 10 skips
                .listener(chunkErrorListener)
                .listener(promotionListener)
                .build();
    }

    /**
     * Defines a step to log all the items that were skipped in the main processing step.
     * This step reads from the list of skipped items collected by the {@link ChunkErrorListener}
     * and uses a {@link MyBatisBatchItemWriter} to update their status in the database.
     *
     * @return A configured {@link Step} for logging skipped items.
     */
    @Bean
    public Step logSkippedItemsStep(ChunkErrorListener chunkErrorListener
            , ItemWriter<ImportLine> skippedItemWriter) {
        return new StepBuilder("logSkippedItemsStep", jobRepository)
                .<ImportLine, ImportLine>chunk(100, transactionManager)
                .reader(new ListItemReader<>(chunkErrorListener.getSkippedItems()))
                .writer(skippedItemWriter)
                .build();
    }

    /**
     * Defines the complete database processing job, which now consists of two steps.
     * 1. {@code processDbStep}: Processes the main data, collecting any skipped items.
     * 2. {@code logSkippedItemsStep}: Persists error information for the items that were skipped.
     *
     * @param processDbStep                     The main processing step.
     * @param logSkippedItemsStep               The step to log skipped items.
     * @param jobCompletionNotificationListener A listener to handle job completion events.
     * @return A configured {@link Job}.
     */
    @Bean
    @JobIdentifier(value = "", description = "", uatIdf = "", prodIdf = "")
    public Job dataBaseProcessingJob(Step processDbStep, Step logSkippedItemsStep, JobCompletionNotificationListener jobCompletionNotificationListener) {
        return new JobBuilder("dataBaseProcessingJob", jobRepository)
                .listener(jobCompletionNotificationListener)
                .start(processDbStep)
                .next(logSkippedItemsStep)
                .build();
    }

    @Bean
    public ItemWriter<ImportLine> skippedItemWriter(SqlSessionFactory sqlSessionFactory) {
        MyBatisBatchItemWriter<ImportLine> writer = new MyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("com.barebonebatch.common.dao.JobDao.updateImportLineWithError");
        return writer;
    }

    /**
     * Creates a listener that promotes the "skippedItems" list from the StepExecutionContext
     * to the JobExecutionContext, making it available to subsequent steps.
     */
    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"skippedItems"});
        return listener;
    }
}
