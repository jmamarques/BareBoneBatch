package com.barebonebatch.common.reader;

import com.barebonebatch.common.domain.ImportLine;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A step-scoped {@link ItemReader} that reads a list of {@link ImportLine} objects
 * from the JobExecutionContext. This is used in a second step to process items
 * that were skipped in a previous step.
 */
@Component
@StepScope
public class SkippedItemsReader implements ItemReader<ImportLine> {

    private ListItemReader<ImportLine> delegate;

    /**
     * Before the step executes, this method retrieves the list of skipped items
     * from the JobExecutionContext and initializes a {@link ListItemReader} delegate
     * to read from that list.
     *
     * @param stepExecution The current step execution, used to access the JobExecutionContext.
     */
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        List<ImportLine> skippedItems = (List<ImportLine>) stepExecution.getJobExecution().getExecutionContext().get("skippedItems");
        if (skippedItems == null) {
            // If no items were skipped, initialize with an empty list.
            skippedItems = new ArrayList<>();
        }
        this.delegate = new ListItemReader<>(skippedItems);
    }

    /**
     * Delegates the read operation to the underlying {@link ListItemReader}.
     *
     * @return The next {@link ImportLine} from the list, or null if the list is exhausted.
     */
    @Override
    public ImportLine read() {
        if (delegate != null) {
            return delegate.read();
        }
        return null;
    }
}
