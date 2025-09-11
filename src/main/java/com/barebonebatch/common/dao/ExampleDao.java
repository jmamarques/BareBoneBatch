package com.barebonebatch.common.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExampleDao {
    /**
     * Inserts a new user record into the database.
     * @param user The user object to insert.
     */
    void insertUser(@Param("user") User user);

    /**
     * Updates an existing user record.
     * @param user The user object with updated values.
     */
    void updateUser(@Param("user") User user);

    /**
     * Deletes a user record by ID.
     * @param user The user object to delete (ID is used for lookup).
     */
    void deleteUser(@Param("user") User user);

    /**
     * Finds a work item based on a file identifier.
     * @param fileId The identifier of the file.
     * @return A list of work items.
     */
    List<String> findWorkByFileId(@Param("fileId") String fileId);

    /**
     * Inserts a record into the import_job table to log the start of a job.
     * @param jobRun A map containing job execution details.
     */
    void insertJobRun(Map<String, Object> jobRun);

    /**
     * Updates a record in the import_job table to log the end of a job.
     * @param jobRun A map containing updated job execution details.
     */
    void updateJobRun(Map<String, Object> jobRun);
}
