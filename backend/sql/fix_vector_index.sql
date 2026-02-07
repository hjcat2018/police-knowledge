-- Fix document_vectors table: Remove unique index and add composite primary key
-- This allows multiple vector chunks per document

-- Drop the unique index that restricts one vector per document
DROP INDEX uk_document_id ON document_vectors;

-- Add composite primary key (document_id, chunk_index)
ALTER TABLE document_vectors ADD PRIMARY KEY (document_id, chunk_index);
