ALTER TABLE book_copies
    DROP CONSTRAINT chk_book_copies_status;

UPDATE book_copies SET status = 'ISSUED' WHERE status = 'LOANED';
UPDATE book_copies SET status = 'DAMAGED' WHERE status = 'MAINTENANCE';

ALTER TABLE book_copies
    ADD CONSTRAINT chk_book_copies_status
    CHECK (status IN ('AVAILABLE', 'ISSUED', 'RESERVED', 'LOST', 'DAMAGED', 'RETIRED'));
