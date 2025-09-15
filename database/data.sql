USE cod;

INSERT INTO cod.WORK_STATUS (WST_IDEN, WST_WORK_IDEN, WST_FILE_IDEN, WST_STAT_CODE, WST_CREA_DATE, WST_BEGI_DATE,
                             WST_ENDX_DATE, WST_ERRO_TEXT)
VALUES (10, 100, 1000, '10', CURRENT_DATE, CURRENT_DATE, NULL, NULL),
       (20, 200, 2000, '10', CURRENT_DATE, CURRENT_DATE, NULL, 'File error');

-- Insert into IMPORT_LINE
INSERT INTO cod.IMPORT_LINE (IML_IDEN, WST_IDEN, IML_NUMB, IML_TEXT, IML_ERRO_TEXT)
VALUES (201, 10, 1, 'Line 1A', NULL),
       (202, 10, 2, 'Line 1B', NULL),
       (203, 10, 3, 'Line 1C', NULL),
       (204, 10, 4, 'Line 1D', NULL),
       (205, 10, 5, 'Line 1E', NULL),
       (206, 20, 1, 'Line 2A', 'Error A'),
       (207, 20, 2, 'Line 2B', 'Error B'),
       (208, 20, 3, 'Line 2C', NULL),
       (209, 20, 4, 'Line 2D', NULL),
       (210, 20, 5, 'Line 2E', NULL);


insert into cod.WORK ("ID", "SYSTEMCODE", "CONTEXT", "WORKCLASSNAME", "DESCRIPTION", "IS_ACTIVE", "IDEN")
values (1, '1000', 'Finance', 'PayrollJob', 'Handles payroll processing', 1, 1001),
       (2, '2000', 'HR', 'RecruitmentJob', 'Manages recruitment workflow', 1, 1002),
       (3, 'XYZ001', 'IT', 'BackupJob', 'Performs system backups', 0, 1003),
       (4, 'ABC003', 'Finance', 'InvoiceJob', 'Processes invoices', 1, 1004),
       (5, 'DEF001', 'Logistics', 'ShippingJob', 'Coordinates shipments', 1, 1005);

commit;