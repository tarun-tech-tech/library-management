-- Render-only demo records for the SmartLib college presentation.
-- This location is enabled by render.yaml and is intentionally not part of
-- the default local migration path.

INSERT INTO books (
    title, author, isbn, category, publisher, publication_year,
    total_copies, available_copies, created_at, updated_at, version
)
SELECT
    'The Design of Everyday Things', 'Don Norman', '9780262525671',
    'Design', 'MIT Press', 2013,
    2, 2, TIMESTAMPTZ '2026-10-01 09:00:00+00', TIMESTAMPTZ '2026-10-01 09:00:00+00', 0
WHERE NOT EXISTS (
    SELECT 1 FROM books WHERE isbn = '9780262525671'
);

INSERT INTO books (
    title, author, isbn, category, publisher, publication_year,
    total_copies, available_copies, created_at, updated_at, version
)
SELECT
    'Clean Code', 'Robert C. Martin', '9780132350884',
    'Software Engineering', 'Prentice Hall', 2008,
    2, 1, TIMESTAMPTZ '2026-10-01 09:05:00+00', TIMESTAMPTZ '2026-10-01 09:05:00+00', 0
WHERE NOT EXISTS (
    SELECT 1 FROM books WHERE isbn = '9780132350884'
);

INSERT INTO books (
    title, author, isbn, category, publisher, publication_year,
    total_copies, available_copies, created_at, updated_at, version
)
SELECT
    'The Pragmatic Programmer', 'David Thomas and Andrew Hunt', '9780135957059',
    'Software Engineering', 'Addison-Wesley', 2019,
    2, 2, TIMESTAMPTZ '2026-10-01 09:10:00+00', TIMESTAMPTZ '2026-10-01 09:10:00+00', 0
WHERE NOT EXISTS (
    SELECT 1 FROM books WHERE isbn = '9780135957059'
);

INSERT INTO members (
    full_name, email, phone, address, registration_date, active, created_at, updated_at
)
SELECT
    'Amina Rahman', 'amina.demo@smartlib.example', '+880 1700 000 101',
    'Dhanmondi, Dhaka', DATE '2026-09-01', TRUE,
    TIMESTAMPTZ '2026-09-01 10:00:00+00', TIMESTAMPTZ '2026-09-01 10:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM members WHERE email = 'amina.demo@smartlib.example'
);

INSERT INTO members (
    full_name, email, phone, address, registration_date, active, created_at, updated_at
)
SELECT
    'Rafi Ahmed', 'rafi.demo@smartlib.example', '+880 1700 000 102',
    'Uttara, Dhaka', DATE '2026-09-03', TRUE,
    TIMESTAMPTZ '2026-09-03 10:00:00+00', TIMESTAMPTZ '2026-09-03 10:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM members WHERE email = 'rafi.demo@smartlib.example'
);

INSERT INTO members (
    full_name, email, phone, address, registration_date, active, created_at, updated_at
)
SELECT
    'Nabila Chowdhury', 'nabila.demo@smartlib.example', '+880 1700 000 103',
    'Mirpur, Dhaka', DATE '2026-09-05', TRUE,
    TIMESTAMPTZ '2026-09-05 10:00:00+00', TIMESTAMPTZ '2026-09-05 10:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM members WHERE email = 'nabila.demo@smartlib.example'
);

INSERT INTO book_copies (book_id, inventory_code, status, acquired_at)
SELECT b.id, 'SMARTLIB-DEMO-TDOE-001', 'AVAILABLE', DATE '2026-09-01'
FROM books b
WHERE b.isbn = '9780262525671'
  AND NOT EXISTS (
      SELECT 1 FROM book_copies WHERE inventory_code = 'SMARTLIB-DEMO-TDOE-001'
  );

INSERT INTO book_copies (book_id, inventory_code, status, acquired_at)
SELECT b.id, 'SMARTLIB-DEMO-TDOE-002', 'AVAILABLE', DATE '2026-09-01'
FROM books b
WHERE b.isbn = '9780262525671'
  AND NOT EXISTS (
      SELECT 1 FROM book_copies WHERE inventory_code = 'SMARTLIB-DEMO-TDOE-002'
  );

INSERT INTO book_copies (book_id, inventory_code, status, acquired_at)
SELECT b.id, 'SMARTLIB-DEMO-CLEAN-001', 'ISSUED', DATE '2026-09-02'
FROM books b
WHERE b.isbn = '9780132350884'
  AND NOT EXISTS (
      SELECT 1 FROM book_copies WHERE inventory_code = 'SMARTLIB-DEMO-CLEAN-001'
  );

INSERT INTO book_copies (book_id, inventory_code, status, acquired_at)
SELECT b.id, 'SMARTLIB-DEMO-CLEAN-002', 'AVAILABLE', DATE '2026-09-02'
FROM books b
WHERE b.isbn = '9780132350884'
  AND NOT EXISTS (
      SELECT 1 FROM book_copies WHERE inventory_code = 'SMARTLIB-DEMO-CLEAN-002'
  );

INSERT INTO book_copies (book_id, inventory_code, status, acquired_at)
SELECT b.id, 'SMARTLIB-DEMO-PRAG-001', 'AVAILABLE', DATE '2026-09-03'
FROM books b
WHERE b.isbn = '9780135957059'
  AND NOT EXISTS (
      SELECT 1 FROM book_copies WHERE inventory_code = 'SMARTLIB-DEMO-PRAG-001'
  );

INSERT INTO book_copies (book_id, inventory_code, status, acquired_at)
SELECT b.id, 'SMARTLIB-DEMO-PRAG-002', 'AVAILABLE', DATE '2026-09-03'
FROM books b
WHERE b.isbn = '9780135957059'
  AND NOT EXISTS (
      SELECT 1 FROM book_copies WHERE inventory_code = 'SMARTLIB-DEMO-PRAG-002'
  );

INSERT INTO loans (
    book_copy_id, member_id, issue_date, due_date, returned_at,
    status, renewal_count, created_at, updated_at
)
SELECT
    c.id, m.id, DATE '2026-09-20', DATE '2026-10-04', NULL,
    'ISSUED', 0, TIMESTAMPTZ '2026-09-20 11:00:00+00', TIMESTAMPTZ '2026-09-20 11:00:00+00'
FROM book_copies c
JOIN members m ON m.email = 'amina.demo@smartlib.example'
WHERE c.inventory_code = 'SMARTLIB-DEMO-CLEAN-001'
  AND NOT EXISTS (
      SELECT 1
      FROM loans existing
      WHERE existing.book_copy_id = c.id
        AND existing.member_id = m.id
        AND existing.issue_date = DATE '2026-09-20'
  );

INSERT INTO loans (
    book_copy_id, member_id, issue_date, due_date, returned_at,
    status, renewal_count, created_at, updated_at
)
SELECT
    c.id, m.id, DATE '2026-08-01', DATE '2026-08-15', DATE '2026-08-22',
    'RETURNED', 0, TIMESTAMPTZ '2026-08-01 11:00:00+00', TIMESTAMPTZ '2026-08-22 14:00:00+00'
FROM book_copies c
JOIN members m ON m.email = 'rafi.demo@smartlib.example'
WHERE c.inventory_code = 'SMARTLIB-DEMO-PRAG-001'
  AND NOT EXISTS (
      SELECT 1
      FROM loans existing
      WHERE existing.book_copy_id = c.id
        AND existing.member_id = m.id
        AND existing.issue_date = DATE '2026-08-01'
  );

INSERT INTO fines (
    loan_id, amount, days_overdue, amount_paid, status, assessed_at, paid_at
)
SELECT
    l.id, 35.00, 7, 0.00, 'UNPAID', TIMESTAMPTZ '2026-08-22 14:05:00+00', NULL
FROM loans l
JOIN book_copies c ON c.id = l.book_copy_id
JOIN members m ON m.id = l.member_id
WHERE c.inventory_code = 'SMARTLIB-DEMO-PRAG-001'
  AND m.email = 'rafi.demo@smartlib.example'
  AND l.issue_date = DATE '2026-08-01'
  AND NOT EXISTS (
      SELECT 1 FROM fines existing WHERE existing.loan_id = l.id
  );

-- Reconcile the aggregate counters with the physical copy states. This only
-- touches the three reserved demo ISBNs and is safe if the migration is
-- restored from a backup before being re-applied.
UPDATE books b
SET total_copies = counts.total_copies,
    available_copies = counts.available_copies,
    updated_at = TIMESTAMPTZ '2026-10-01 09:15:00+00'
FROM (
    SELECT c.book_id,
           COUNT(*)::INTEGER AS total_copies,
           COUNT(*) FILTER (WHERE c.status = 'AVAILABLE')::INTEGER AS available_copies
    FROM book_copies c
    GROUP BY c.book_id
) counts
WHERE b.id = counts.book_id
  AND b.isbn IN ('9780262525671', '9780132350884', '9780135957059');
