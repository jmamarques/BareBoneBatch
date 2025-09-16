package com.barebonebatch.common.listener;

import com.barebonebatch.common.dao.JobDao;
import com.barebonebatch.common.domain.ImportLine;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.SkipListener;

/**
 * A generic listener that logs items skipped during a batch step.
 * This listener can be used to record problematic items for later analysis without
 * interrupting the batch job.
 */
@Log4j2
public class GenericSkipListener implements SkipListener<ImportLine, Object> {

    private JobDao jobDao;

    public GenericSkipListener(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    /**
     * Called when an item is skipped during the read phase.
     *
     * @param t The throwable that caused the skip.
     */
    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Skipped an item during read due to: {}", t.getMessage());
    }

    /**
     * Called when an item is skipped during the process phase.
     * This is the most common place to handle bad data from a source file.
     *
     * @param item The item that was being processed when the error occurred.
     * @param t    The throwable that caused the skip (e.g., a validation exception).
     */
    @Override
    public void onSkipInProcess(ImportLine item, Throwable t) {
        // Log the raw text of the line and the specific error
        log.warn("Skipping line during processing: [Line: {}] due to [Error: {}]", item.getImlText(), t.getMessage());

        item.setImlErroText(StringUtils.truncate(t.getMessage(), 1000));
        jobDao.updateImportLineWithError(item);
    }

    /**
     * Called when an item is skipped during the write phase.
     *
     * @param item The item that was being written.
     * @param t    The throwable that caused the skip.
     */
    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("Skipped an item during write: [{}] due to: {}", item, t.getMessage());
    }
}
