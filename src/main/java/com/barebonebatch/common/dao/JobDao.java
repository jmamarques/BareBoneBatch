package com.barebonebatch.common.dao;

import com.barebonebatch.common.domain.ImportLine;
import com.barebonebatch.common.domain.Mapping;
import com.barebonebatch.common.domain.Work;
import com.barebonebatch.common.domain.WorkStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobDao {
    WorkStatus getNextWorkStatusPending();
    List<WorkStatus> getWorkStatusByWstIden(Long wstIden);
    List<ImportLine> getLinesByWstIden(Long wstIden);
    List<Work> getWorkByFileIden(String fileIden);

    Mapping getMappingById(String id);

    void updateWorkStatus(WorkStatus jobRequest);

    void updateWorkStatusEnd(WorkStatus jobRequest);
}
