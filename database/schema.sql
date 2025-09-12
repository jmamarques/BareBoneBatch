USE cod;

CREATE TABLE IF NOT EXISTS crypto_transaction
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_utc          TIMESTAMP            NOT NULL,
    pair              VARCHAR(255)         NOT NULL,
    side              ENUM ('BUY', 'SELL') NOT NULL,
    price             DECIMAL(20, 10)      NOT NULL,
    executed_amount   DECIMAL(20, 10),
    executed_currency VARCHAR(10),
    amount_amount     DECIMAL(20, 10),
    amount_currency   VARCHAR(10),
    fee_amount        DECIMAL(20, 10),
    fee_currency      VARCHAR(10)
);

ALTER TABLE crypto_transaction
    ADD CONSTRAINT unique_transaction UNIQUE (date_utc, pair, side, price, executed_amount, executed_currency,
                                              amount_amount, amount_currency, fee_amount, fee_currency);

CREATE TABLE  IF NOT EXISTS BATCH_JOB_INSTANCE  (
                                     JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
                                     VERSION BIGINT ,
                                     JOB_NAME VARCHAR(100) NOT NULL,
                                     JOB_KEY VARCHAR(32) NOT NULL,
                                     constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE  IF NOT EXISTS BATCH_JOB_EXECUTION  (
                                      JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                      VERSION BIGINT  ,
                                      JOB_INSTANCE_ID BIGINT NOT NULL,
                                      CREATE_TIME DATETIME(6) NOT NULL,
                                      START_TIME DATETIME(6) DEFAULT NULL ,
                                      END_TIME DATETIME(6) DEFAULT NULL ,
                                      STATUS VARCHAR(10) ,
                                      EXIT_CODE VARCHAR(2500) ,
                                      EXIT_MESSAGE VARCHAR(2500) ,
                                      LAST_UPDATED DATETIME(6),
                                      constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                                          references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE  IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS  (
                                             JOB_EXECUTION_ID BIGINT NOT NULL ,
                                             PARAMETER_NAME VARCHAR(100) NOT NULL ,
                                             PARAMETER_TYPE VARCHAR(100) NOT NULL ,
                                             PARAMETER_VALUE VARCHAR(2500) ,
                                             IDENTIFYING CHAR(1) NOT NULL ,
                                             constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                                                 references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE  IF NOT EXISTS BATCH_STEP_EXECUTION  (
                                       STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                       VERSION BIGINT NOT NULL,
                                       STEP_NAME VARCHAR(100) NOT NULL,
                                       JOB_EXECUTION_ID BIGINT NOT NULL,
                                       CREATE_TIME DATETIME(6) NOT NULL,
                                       START_TIME DATETIME(6) DEFAULT NULL ,
                                       END_TIME DATETIME(6) DEFAULT NULL ,
                                       STATUS VARCHAR(10) ,
                                       COMMIT_COUNT BIGINT ,
                                       READ_COUNT BIGINT ,
                                       FILTER_COUNT BIGINT ,
                                       WRITE_COUNT BIGINT ,
                                       READ_SKIP_COUNT BIGINT ,
                                       WRITE_SKIP_COUNT BIGINT ,
                                       PROCESS_SKIP_COUNT BIGINT ,
                                       ROLLBACK_COUNT BIGINT ,
                                       EXIT_CODE VARCHAR(2500) ,
                                       EXIT_MESSAGE VARCHAR(2500) ,
                                       LAST_UPDATED DATETIME(6),
                                       constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
                                           references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE  IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT  (
                                               STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                               SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                               SERIALIZED_CONTEXT TEXT ,
                                               constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
                                                   references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE  IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT  (
                                              JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                              SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                              SERIALIZED_CONTEXT TEXT ,
                                              constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
                                                  references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE  IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ (
                                          ID BIGINT NOT NULL,
                                          UNIQUE_KEY CHAR(1) NOT NULL,
                                          constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE  IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ (
                                         ID BIGINT NOT NULL,
                                         UNIQUE_KEY CHAR(1) NOT NULL,
                                         constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE  IF NOT EXISTS BATCH_JOB_SEQ (
                               ID BIGINT NOT NULL,
                               UNIQUE_KEY CHAR(1) NOT NULL,
                               constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_SEQ);


-- ====================
-- Grant Permissions
-- ====================

-- Grant full privileges (read/write) to writer_user
GRANT ALL PRIVILEGES ON cod.crypto_transaction TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_JOB_INSTANCE TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_JOB_EXECUTION TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_JOB_EXECUTION_PARAMS TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_STEP_EXECUTION TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_STEP_EXECUTION_CONTEXT TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_JOB_EXECUTION_CONTEXT TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_STEP_EXECUTION_SEQ TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_JOB_EXECUTION_SEQ TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.BATCH_JOB_SEQ TO 'writer_user'@'%';

-- Grant read-only (SELECT) permissions to reader_user
GRANT SELECT ON cod.crypto_transaction TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_JOB_INSTANCE TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_JOB_EXECUTION TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_JOB_EXECUTION_PARAMS TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_STEP_EXECUTION TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_STEP_EXECUTION_CONTEXT TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_JOB_EXECUTION_CONTEXT TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_STEP_EXECUTION_SEQ TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_JOB_EXECUTION_SEQ TO 'reader_user'@'%';
GRANT SELECT ON cod.BATCH_JOB_SEQ TO 'reader_user'@'%';

-- Apply privileges
FLUSH PRIVILEGES;