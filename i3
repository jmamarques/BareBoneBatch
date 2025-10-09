Here's a comprehensive custom MyBatis batch writer for Oracle schema:

🏆 Custom MyBatis Batch Writer for Oracle

1. Base Custom Batch Writer

```java
@Component
public class CustomMyBatisBatchWriter<T> implements ItemWriter<T> {
    
    protected final SqlSessionTemplate sqlSessionTemplate;
    protected final String statementId;
    protected final Class<T> type;
    protected final int batchSize;
    
    public CustomMyBatisBatchWriter(SqlSessionTemplate sqlSessionTemplate, 
                                   String statementId, Class<T> type, int batchSize) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.statementId = statementId;
        this.type = type;
        this.batchSize = batchSize;
    }
    
    @Override
    @Transactional
    public void write(List<? extends T> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // Process in batches to avoid Oracle limitations
        List<List<? extends T>> batches = partitionList(items, batchSize);
        
        for (int i = 0; i < batches.size(); i++) {
            List<? extends T> batch = batches.get(i);
            try {
                executeBatch(batch);
                System.out.printf("Successfully wrote batch %d/%d with %d items%n", 
                    i + 1, batches.size(), batch.size());
            } catch (Exception e) {
                System.err.printf("Failed to write batch %d: %s%n", i + 1, e.getMessage());
                throw e;
            }
        }
    }
    
    protected void executeBatch(List<? extends T> batch) {
        // Use MyBatis batch session for optimal performance
        SqlSession batchSession = sqlSessionTemplate.getSqlSessionFactory()
                .openSession(ExecutorType.BATCH);
        
        try {
            for (T item : batch) {
                batchSession.insert(statementId, item);
            }
            batchSession.commit();
            batchSession.flushStatements();
        } catch (Exception e) {
            batchSession.rollback();
            throw new RuntimeException("Batch insert failed", e);
        } finally {
            batchSession.close();
        }
    }
    
    protected <U> List<List<U>> partitionList(List<U> list, int batchSize) {
        List<List<U>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }
}
```

2. Oracle-Specific Account Writer

```java
@Component
public class OracleAccountWriter extends CustomMyBatisBatchWriter<Account> {
    
    private static final int ORACLE_BATCH_SIZE = 1000; // Oracle performs best with 100-1000 items
    
    @Autowired
    public OracleAccountWriter(SqlSessionTemplate sqlSessionTemplate) {
        super(sqlSessionTemplate, "AccountMapper.batchInsert", Account.class, ORACLE_BATCH_SIZE);
    }
    
    @Override
    protected void executeBatch(List<? extends Account> batch) {
        SqlSession batchSession = null;
        try {
            batchSession = sqlSessionTemplate.getSqlSessionFactory()
                    .openSession(ExecutorType.BATCH);
            
            // Oracle-specific batch optimization
            for (Account account : batch) {
                // Pre-process for Oracle
                preProcessForOracle(account);
                batchSession.insert(statementId, account);
            }
            
            batchSession.commit();
            batchSession.flushStatements();
            
        } catch (Exception e) {
            if (batchSession != null) {
                batchSession.rollback();
            }
            handleOracleBatchException(e, batch);
        } finally {
            if (batchSession != null) {
                batchSession.close();
            }
        }
    }
    
    private void preProcessForOracle(Account account) {
        // Handle Oracle-specific data types and constraints
        if (account.getCreatedDate() == null) {
            account.setCreatedDate(LocalDateTime.now());
        }
        
        // Ensure BigDecimal precision for Oracle NUMBER type
        if (account.getBalance() != null) {
            account.setBalance(account.getBalance().setScale(2, RoundingMode.HALF_UP));
        }
    }
    
    private void handleOracleBatchException(Exception e, List<? extends Account> failedBatch) {
        if (e.getCause() instanceof SQLException) {
            SQLException sqlEx = (SQLException) e.getCause();
            int oracleErrorCode = sqlEx.getErrorCode();
            
            switch (oracleErrorCode) {
                case 1: // Unique constraint violation
                    System.err.println("Oracle unique constraint violation");
                    break;
                case 1400: // NOT NULL constraint violation
                    System.err.println("Oracle NOT NULL constraint violation");
                    break;
                case 2291: // Integrity constraint violation
                    System.err.println("Oracle foreign key violation");
                    break;
                case 12899: // Value too large for column
                    System.err.println("Oracle column size exceeded");
                    break;
                default:
                    System.err.println("Oracle error code: " + oracleErrorCode);
            }
        }
        throw new RuntimeException("Oracle batch insert failed", e);
    }
}
```

3. Composite Writer for Multiple Entities

```java
@Component
public class OracleCompositeWriter implements ItemWriter<ProcessResult> {
    
    @Autowired
    private OracleAccountWriter accountWriter;
    
    @Autowired
    private OracleUserWriter userWriter;
    
    @Autowired
    private OracleAuditWriter auditWriter;
    
    @Override
    @Transactional
    public void write(List<? extends ProcessResult> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // Extract entities in batches
        List<Account> accounts = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<Audit> audits = new ArrayList<>();
        
        for (ProcessResult result : items) {
            if (result.getAccount() != null) {
                accounts.add(result.getAccount());
            }
            if (result.getUser() != null) {
                users.add(result.getUser());
            }
            if (result.getAudits() != null) {
                audits.addAll(result.getAudits());
            }
        }
        
        // Write in optimal order (parents first, then children)
        if (!accounts.isEmpty()) {
            accountWriter.write(accounts);
        }
        if (!users.isEmpty()) {
            userWriter.write(users);
        }
        if (!audits.isEmpty()) {
            auditWriter.write(audits);
        }
        
        logWriteStatistics(items.size(), accounts.size(), users.size(), audits.size());
    }
    
    private void logWriteStatistics(int totalItems, int accounts, int users, int audits) {
        System.out.printf("Write Statistics: Total=%d, Accounts=%d, Users=%d, Audits=%d%n",
                totalItems, accounts, users, audits);
    }
}
```

4. MyBatis Mapper XML for Oracle

AccountMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.mapper.AccountMapper">
    
    <!-- Single insert for fallback -->
    <insert id="insert" parameterType="Account">
        INSERT INTO accounts (
            account_id, account_number, balance, status, 
            created_date, modified_date, version
        ) VALUES (
            ACCOUNTS_SEQ.NEXTVAL,
            #{accountNumber},
            #{balance},
            #{status},
            #{createdDate},
            #{modifiedDate},
            #{version}
        )
        <selectKey keyProperty="accountId" resultType="Long" order="BEFORE">
            SELECT ACCOUNTS_SEQ.NEXTVAL FROM DUAL
        </selectKey>
    </insert>
    
    <!-- Oracle-optimized batch insert using INSERT ALL -->
    <insert id="batchInsert" parameterType="java.util.List">
        INSERT ALL
        <foreach collection="list" item="account">
            INTO accounts (
                account_id, account_number, balance, status, 
                created_date, modified_date, version
            ) VALUES (
                ACCOUNTS_SEQ.NEXTVAL,
                #{account.accountNumber},
                #{account.balance},
                #{account.status},
                #{account.createdDate, jdbcType=TIMESTAMP},
                #{account.modifiedDate, jdbcType=TIMESTAMP},
                #{account.version}
            )
        </foreach>
        SELECT * FROM DUAL
    </insert>
    
    <!-- Alternative: Using UNION ALL for very large batches -->
    <insert id="batchInsertUnion" parameterType="java.util.List">
        INSERT INTO accounts (
            account_id, account_number, balance, status, created_date
        )
        SELECT ACCOUNTS_SEQ.NEXTVAL, account_data.* FROM (
            <foreach collection="list" item="account" separator=" UNION ALL ">
                SELECT 
                    #{account.accountNumber} as account_number,
                    #{account.balance} as balance,
                    #{account.status} as status,
                    #{account.createdDate} as created_date
                FROM DUAL
            </foreach>
        ) account_data
    </insert>
    
    <!-- Batch update for existing records -->
    <update id="batchUpdate" parameterType="java.util.List">
        <foreach collection="list" item="account" separator=";" open="BEGIN" close="; END;">
            UPDATE accounts 
            SET 
                balance = #{account.balance},
                status = #{account.status},
                modified_date = #{account.modifiedDate},
                version = version + 1
            WHERE account_number = #{account.accountNumber}
            AND version = #{account.version}
        </foreach>
    </update>
    
    <!-- Merge (UPSERT) operation for Oracle -->
    <update id="mergeAccounts" parameterType="java.util.List">
        <foreach collection="list" item="account" separator=";" open="BEGIN" close="; END;">
            MERGE INTO accounts a
            USING (SELECT #{account.accountNumber} as account_number FROM DUAL) src
            ON (a.account_number = src.account_number)
            WHEN MATCHED THEN
                UPDATE SET 
                    a.balance = #{account.balance},
                    a.status = #{account.status},
                    a.modified_date = #{account.modifiedDate},
                    a.version = a.version + 1
            WHEN NOT MATCHED THEN
                INSERT (account_id, account_number, balance, status, created_date, version)
                VALUES (ACCOUNTS_SEQ.NEXTVAL, #{account.accountNumber}, #{account.balance}, 
                       #{account.status}, #{account.createdDate}, 1)
        </foreach>
    </update>
</mapper>
```

AuditMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.mapper.AuditMapper">
    
    <!-- High-performance batch insert for audit records -->
    <insert id="batchInsert" parameterType="java.util.List">
        INSERT INTO audit_trail (
            audit_id, entity_type, entity_id, action_type, 
            action_timestamp, user_id, old_values, new_values
        )
        SELECT AUDIT_SEQ.NEXTVAL, A.* FROM (
            <foreach collection="list" item="audit" separator=" UNION ALL ">
                SELECT 
                    #{audit.entityType} as entity_type,
                    #{audit.entityId} as entity_id,
                    #{audit.actionType} as action_type,
                    #{audit.actionTimestamp, jdbcType=TIMESTAMP} as action_timestamp,
                    #{audit.userId} as user_id,
                    #{audit.oldValues, jdbcType=CLOB} as old_values,
                    #{audit.newValues, jdbcType=CLOB} as new_values
                FROM DUAL
            </foreach>
        ) A
    </insert>
</mapper>
```

5. Writer Factory for Easy Management

```java
@Component
public class OracleWriterFactory {
    
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    
    private final Map<String, CustomMyBatisBatchWriter<?>> writerCache = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T> CustomMyBatisBatchWriter<T> getWriter(Class<T> type, String statementId) {
        String key = type.getSimpleName() + ":" + statementId;
        
        return (CustomMyBatisBatchWriter<T>) writerCache.computeIfAbsent(key, k -> {
            switch (type.getSimpleName()) {
                case "Account":
                    return new OracleAccountWriter(sqlSessionTemplate);
                case "User":
                    return new OracleUserWriter(sqlSessionTemplate);
                case "Audit":
                    return new OracleAuditWriter(sqlSessionTemplate);
                default:
                    return new CustomMyBatisBatchWriter<>(sqlSessionTemplate, 
                        statementId, type, 1000);
            }
        });
    }
    
    public CustomMyBatisBatchWriter<Account> getAccountWriter() {
        return getWriter(Account.class, "AccountMapper.batchInsert");
    }
    
    public CustomMyBatisBatchWriter<User> getUserWriter() {
        return getWriter(User.class, "UserMapper.batchInsert");
    }
    
    public CustomMyBatisBatchWriter<Audit> getAuditWriter() {
        return getWriter(Audit.class, "AuditMapper.batchInsert");
    }
}
```

6. Configuration

```java
@Configuration
public class OracleBatchWriterConfig {
    
    @Bean
    public OracleAccountWriter accountWriter(SqlSessionTemplate sqlSessionTemplate) {
        return new OracleAccountWriter(sqlSessionTemplate);
    }
    
    @Bean
    public OracleUserWriter userWriter(SqlSessionTemplate sqlSessionTemplate) {
        return new OracleUserWriter(sqlSessionTemplate);
    }
    
    @Bean
    public OracleAuditWriter auditWriter(SqlSessionTemplate sqlSessionTemplate) {
        return new OracleAuditWriter(sqlSessionTemplate);
    }
    
    @Bean
    public OracleCompositeWriter compositeWriter() {
        return new OracleCompositeWriter();
    }
    
    @Bean
    public OracleWriterFactory writerFactory() {
        return new OracleWriterFactory();
    }
}
```

7. Usage in Batch Step

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean
    public Step processingStep(ItemReader<ImportLine> reader,
                              ItemProcessor<ImportLine, ProcessResult> processor,
                              OracleCompositeWriter compositeWriter) {
        return stepBuilderFactory.get("processingStep")
                .<ImportLine, ProcessResult>chunk(500) // Optimal chunk size for Oracle
                .reader(reader)
                .processor(processor)
                .writer(compositeWriter)
                .faultTolerant()
                .skipLimit(100)
                .skip(DataIntegrityViolationException.class)
                .skip(OracleSQLException.class)
                .retryLimit(3)
                .retry(DeadlockLoserDataAccessException.class)
                .build();
    }
}
```

🚀 Key Oracle Optimizations:

1. ✅ Batch Size Control: Optimal 1000 records per batch for Oracle
2. ✅ Oracle Sequences: Proper sequence handling with SELECT KEY
3. ✅ INSERT ALL: Oracle-specific batch insert syntax
4. ✅ Error Handling: Oracle error code specific handling
5. ✅ Data Type Mapping: Proper Oracle type conversions
6. ✅ Transaction Management: Optimal batch commit strategy

This custom writer will provide significantly better performance with Oracle compared to generic MyBatis batch writers!