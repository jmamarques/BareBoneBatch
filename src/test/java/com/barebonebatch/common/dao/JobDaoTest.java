package com.barebonebatch.common.dao;

import com.barebonebatch.common.domain.WorkStatus;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobDaoTest {

    @Autowired
    private JobDao jobDao;

    @Test
    void getWorkStatusPending() {
        // given
        // assume there is a pending status in the test database

        // when
        WorkStatus workStatus = jobDao.getNextWorkStatusPending();

        // then
        assertThat(workStatus).isNotNull();
    }
}
