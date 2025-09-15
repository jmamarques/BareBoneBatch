package com.barebonebatch.common.dao;

import com.barebonebatch.common.domain.WorkStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobDao {
    List<WorkStatus> getWorkStatusPending();
}
