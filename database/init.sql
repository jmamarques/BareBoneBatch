CREATE DATABASE IF NOT EXISTS cod;

-- Switch to 'cod' database
USE cod;

-- Create the writer user with full privileges on the 'cod' database
CREATE USER 'writer_user'@'%' IDENTIFIED BY 'writerPassword';
GRANT ALL PRIVILEGES ON cod.* TO 'writer_user'@'%';

-- Create the reader user with read-only privileges on the 'cod' database
CREATE USER 'reader_user'@'%' IDENTIFIED BY 'readerPassword';
GRANT SELECT ON cod.* TO 'reader_user'@'%';

-- Apply privileges
FLUSH PRIVILEGES;