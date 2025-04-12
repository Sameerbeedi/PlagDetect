CREATE DATABASE IF NOT EXISTS PlagDetect;
USE PlagDetect;


CREATE TABLE IF NOT EXISTS uploaded_files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255),
    format varchar(50),
    similarity_score float,
    flagged boolean default false,
    similar_to varchar(100)
);

ALTER TABLE uploaded_files MODIFY COLUMN format LONGTEXT;