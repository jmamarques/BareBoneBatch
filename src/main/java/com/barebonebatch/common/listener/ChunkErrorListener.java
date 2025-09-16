package com.barebonebatch.common.listener;

import com.barebonebatch.common.domain.ImportLine;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A listener that captures items skipped during a chunk-oriented step.
 * It implements both {@link SkipListener} to catch skipped items and
 * {@link StepExecutionListener} to save the collected items into the
 * StepExecutionContext before the step completes.
 */
@Getter
@Component
@Log4j2
public class ChunkErrorListener implements SkipListener<ImportLine, Object> {

    private final List<ImportLine> skippedItems = new ArrayList<>();

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Skipped item during read", t);
    }

    @Override
    public void onSkipInProcess(ImportLine item, Throwable t) {
        log.warn("Skipping item {} during processing due to: {}", item.getImlIden(), t.getMessage());
        item.setImlErroText(StringUtils.truncate(t.getMessage(), 1000));
        skippedItems.add(item);
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        if (item instanceof ImportLine) {
            log.warn("Skipping item {} during write due to: {}", ((ImportLine) item).getImlIden(), t.getMessage());
            ((ImportLine) item).setImlErroText(StringUtils.truncate(t.getMessage(), 1000));
            skippedItems.add((ImportLine) item);
        }
    }

}
