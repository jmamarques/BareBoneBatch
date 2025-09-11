package com.barebonebatch.common.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;
import java.io.File;

public class FileJobParametersValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        // Get the 'inputFile' parameter
        String inputFile = parameters.getString("inputFile");

        // Check if the parameter exists
        if (!StringUtils.hasText(inputFile)) {
            throw new JobParametersInvalidException("The 'inputFile' job parameter is missing or empty.");
        }

        // Check if the file exists
        File file = new File(inputFile);
        if (!file.exists()) {
            throw new JobParametersInvalidException("The input file '" + inputFile + "' does not exist.");
        }
    }
}
