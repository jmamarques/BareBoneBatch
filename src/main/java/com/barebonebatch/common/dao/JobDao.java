package com.barebonebatch.common.dao;

import com.barebonebatch.common.domain.ImportLine;
import com.barebonebatch.common.domain.Mapping;
import com.barebonebatch.common.domain.Work;
import com.barebonebatch.common.domain.WorkStatus;
import com.barebonebatch.dbprocessing.Dummy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobDao {
    WorkStatus getNextWorkStatusPending();
    Optional<WorkStatus> getWorkStatusByWstIden(Long wstIden);
    List<ImportLine> getLinesByWstIden(Long wstIden);
    List<Work> getWorkByFileIden(String fileIden);

    Mapping getMappingById(String id);

    void updateWorkStatus(WorkStatus jobRequest);

    void updateWorkStatusEnd(WorkStatus jobRequest);
    void dummyinsert(Dummy dummy);

    void updateImportLineWithError(ImportLine item);
}
