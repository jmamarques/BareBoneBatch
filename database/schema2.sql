USE cod;


CREATE TABLE WORK_STATUS (
                                 WST_IDEN INT NOT NULL,
                                 WST_WORK_IDEN VARCHAR(40),
                                 WST_FILE_IDEN VARCHAR(60) NOT NULL,
                                 WST_STAT_CODE INT NOT NULL,
                                 WST_CREA_DATE DATETIME,
                                 WST_BEGI_DATE DATETIME,
                                 WST_ENDX_DATE DATETIME,
                                 WST_ERRO_TEXT VARCHAR(255),
                                 PRIMARY KEY (WST_IDEN)
);

CREATE TABLE MAPPING (
                             ID VARCHAR(100),
                             MAPPING_TYPE VARCHAR(100),
                             IDEN INT(12) NOT NULL,
                             PRIMARY KEY (IDEN),
                             UNIQUE KEY UQ_ID_MAPPING_TYPE (ID, MAPPING_TYPE)
);

CREATE TABLE WORK (
                          ID VARCHAR(100),
                          SYSTEMCODE VARCHAR(100),
                          CONTEXT VARCHAR(100),
                          WORKCLASSNAME VARCHAR(100),
                          DESCRIPTION VARCHAR(100),
                          IS_ACTIVE VARCHAR(1) NOT NULL DEFAULT 'Y',
                          IDEN INT NOT NULL,
                          PRIMARY KEY (IDEN),
                          CHECK (IS_ACTIVE IN ('Y', 'N'))
);

CREATE TABLE MAPPING_FIELDS (
                                    MAPPINGFK INT(12),
                                    IDEN INT(12) NOT NULL,
                                    ID VARCHAR(100),
                                    DESCRIPTION VARCHAR(200),
                                    PROPERTY VARCHAR(200),
                                    TRANSFORMER VARCHAR(200) DEFAULT NULL,
                                    PATTERN VARCHAR(200) DEFAULT NULL,
                                    MANDATORY VARCHAR(5) NOT NULL DEFAULT 'N',
                                    ENABLE VARCHAR(5),
                                    TYPE VARCHAR(100),
                                    `OFFSET` INT(5),
                                    LENGTH INT(5),
                                    PRIMARY KEY (IDEN),
                                    CHECK (MANDATORY IN ('Y', 'N')),
                                    CHECK (ENABLE IN ('Y', 'N')),
                                    CHECK (`OFFSET` >= 0),
                                    CHECK (LENGTH > 0),
                                    FOREIGN KEY (MAPPINGFK) REFERENCES MAPPING(IDEN)
);

CREATE TABLE DUMMY (
                               text1 VARCHAR(100),
                               text2 VARCHAR(100),
                               text3 VARCHAR(100)
);

CREATE TABLE IMPORT_LINE (
                                 IML_IDEN INT NOT NULL,
                                 WST_IDEN INT NOT NULL,
                                 IML_NUMB INT NOT NULL,
                                 IML_TEXT VARCHAR(2000),
                                 IML_ERRO_TEXT VARCHAR(1000),
                                 PRIMARY KEY (IML_IDEN),
                                 FOREIGN KEY (WST_IDEN) REFERENCES WORK_STATUS(WST_IDEN)
);


-- Grant read-only (SELECT) permissions to reader_user
GRANT SELECT ON cod.IMPORT_LINE TO 'reader_user'@'%';
GRANT SELECT ON cod.WORK_STATUS TO 'reader_user'@'%';
GRANT SELECT ON cod.MAPPING TO 'reader_user'@'%';
GRANT SELECT ON cod.MAPPING_FIELDS TO 'reader_user'@'%';
GRANT SELECT ON cod.WORK TO 'reader_user'@'%';
GRANT SELECT ON cod.IMPORT_LINE TO 'writer_user'@'%';
GRANT SELECT ON cod.WORK_STATUS TO 'writer_user'@'%';
GRANT SELECT ON cod.MAPPING TO 'writer_user'@'%';
GRANT SELECT ON cod.MAPPING_FIELDS TO 'writer_user'@'%';
GRANT SELECT ON cod.WORK TO 'writer_user'@'%';
GRANT SELECT ON cod.DUMMY TO 'reader_user'@'%';
GRANT SELECT ON cod.DUMMY TO 'writer_user'@'%';

-- Apply privileges
FLUSH PRIVILEGES;