-- ============================================================
-- EMPLOYEES
-- ============================================================
INSERT INTO employees (birth_date, email, name, password, phone)
VALUES ('1990-05-15', 'leosaz06175@gmail.com', 'John Doe', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-123-4567'),
       ('1985-09-20', 'jane.smith@email.com', 'Jane Smith', '$2a$12$aiwlVeTro/kEp2oR1rZkhenc9V8BWNgyPeCdr/13.DRJjgx46ISbG', '555-987-6543'),
       ('1978-03-08', 'bob.jones@email.com', 'Bob Jones', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-321-6789'),
       ('1982-11-25', 'alice.white@email.com', 'Alice White', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-876-5432'),
       ('1995-07-12', 'mike.wilson@email.com', 'Mike Wilson', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-234-5678'),
       ('1989-01-30', 'sara.brown@email.com', 'Sara Brown', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-876-5433'),
       ('1975-06-18', 'tom.jenkins@email.com', 'Tom Jenkins', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-345-6789'),
       ('1987-12-04', 'lisa.taylor@email.com', 'Lisa Taylor', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-789-0123'),
       ('1992-08-22', 'david.wright@email.com', 'David Wright', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-456-7890'),
       ('1980-04-10', 'emily.harris@email.com', 'Emily Harris', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', '555-098-7654');

-- ============================================================
-- CLIENTS
-- ============================================================
INSERT INTO clients (balance, email, name, password, active)
VALUES (1000.00, 'client1@example.com', 'Medelyn Wright', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (1500.50, 'client2@example.com', 'Landon Phillips', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (800.75,  'client3@example.com', 'Harmony Mason', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (1200.25, 'client4@example.com', 'Archer Harper', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (900.80,  'client5@example.com', 'Kira Jacobs', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (1100.60, 'client6@example.com', 'Maximus Kelly', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (1300.45, 'client7@example.com', 'Sierra Mitchell', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (950.30,  'client8@example.com', 'Quinton Saunders', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', false),
       (1050.90, 'client9@example.com', 'Amina Clarke', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true),
       (880.20,  'leosaz06173@gmail.com', 'Leonid Sazonov', '$2a$12$h3PyBgsInXXr/j03N5j7ueuZISsPplNauAvFRvZujR9XxowhLnKKu', true);

-- ============================================================
-- BOOKS (original 10 + 10 more)
-- ============================================================
INSERT INTO books (name, genre, age_group, price, publication_date, author, pages, characteristics, description, language)
VALUES
    -- Original batch
    ('The Hidden Treasure',     'Adventure',        'ADULT', 24.99, '2018-05-15', 'Emily White',    400, 'Mysterious journey',        'An enthralling adventure of discovery',          'ENGLISH'),
    ('Echoes of Eternity',      'Fantasy',          'TEEN',  16.50, '2011-01-15', 'Daniel Black',   350, 'Magical realms',            'A spellbinding tale of magic and destiny',       'ENGLISH'),
    ('Whispers in the Shadows', 'Mystery',          'ADULT', 29.95, '2018-08-11', 'Sophia Green',   450, 'Intriguing suspense',       'A gripping mystery that keeps you guessing',     'ENGLISH'),
    ('The Starlight Sonata',    'Romance',          'ADULT', 21.75, '2011-05-15', 'Michael Rose',   320, 'Heartwarming love story',   'A beautiful journey of love and passion',        'ENGLISH'),
    ('Beyond the Horizon',      'Science Fiction',  'CHILD', 18.99, '2004-05-15', 'Alex Carter',    280, 'Interstellar adventure',    'An epic sci-fi adventure beyond the stars',      'ENGLISH'),
    ('Dancing with Shadows',    'Thriller',         'ADULT', 26.50, '2015-05-15', 'Olivia Smith',   380, 'Suspenseful twists',        'A thrilling tale of danger and intrigue',        'ENGLISH'),
    ('Voices in the Wind',      'Historical Fiction','ADULT',32.00, '2017-05-15', 'William Turner', 500, 'Rich historical setting',   'A compelling journey through time',              'ENGLISH'),
    ('Serenade of Souls',       'Fantasy',          'TEEN',  15.99, '2013-05-15', 'Isabella Reed',  330, 'Enchanting realms',         'A magical fantasy filled with wonder',           'ENGLISH'),
    ('Silent Whispers',         'Mystery',          'ADULT', 27.50, '2021-05-15', 'Benjamin Hall',  420, 'Intricate detective work',  'A mystery that keeps you on the edge',           'ENGLISH'),
    ('Whirlwind Romance',       'Romance',          'OTHER', 23.25, '2022-05-15', 'Emma Turner',    360, 'Passionate love affair',    'A romance that sweeps you off your feet',        'ENGLISH'),
    -- Extended batch
    ('The Forgotten Kingdom',   'Fantasy',          'TEEN',  19.99, '2016-03-22', 'Clara Moon',     410, 'Epic world-building',       'A sweeping fantasy across forgotten lands',      'ENGLISH'),
    ('Midnight Protocol',       'Thriller',         'ADULT', 28.00, '2020-11-10', 'James Harlow',   370, 'High-stakes espionage',     'A pulse-pounding spy thriller',                  'ENGLISH'),
    ('Stars and Satellites',    'Science Fiction',  'TEEN',  17.50, '2019-07-04', 'Nora Klein',     300, 'Near-future dystopia',      'A thought-provoking tale of technology',         'ENGLISH'),
    ('The Amber Compass',       'Adventure',        'CHILD', 13.99, '2010-09-01', 'Oscar Lane',     220, 'Treasure hunt',             'A thrilling quest for a young explorer',          'ENGLISH'),
    ('Letters to Nowhere',      'Historical Fiction','ADULT',34.50, '2014-02-14', 'Chloe Dubois',   540, 'WWI correspondence',        'A moving story told through wartime letters',     'FRENCH'),
    ('Burning Tundra',          'Thriller',         'ADULT', 25.99, '2023-01-18', 'Ivan Petrov',    395, 'Arctic survival',           'A race for survival in the frozen north',         'ENGLISH'),
    ('The Glass Menagerie Code','Mystery',          'OTHER', 22.00, '2009-06-30', 'Diana Cross',    310, 'Art-world intrigue',        'A twisty mystery set in a glittering gallery',   'ENGLISH'),
    ('Moonlit Waltz',           'Romance',          'ADULT', 20.50, '2020-02-28', 'Sofia Reyes',    295, 'Second-chance romance',     'Love rekindled under the silver moon',           'SPANISH'),
    ('Junior Galaxy Rangers',   'Science Fiction',  'CHILD', 12.99, '2007-11-11', 'Tom Briggs',     180, 'Space exploration for kids','An action-packed ride across the cosmos',         'ENGLISH'),
    ('Shadow Parliament',       'Historical Fiction','ADULT',36.00, '2021-09-09', 'Marcus Wren',    580, 'Political intrigue',        'Power, betrayal and revolution in 18th-c. Europe','ENGLISH');

-- ============================================================
-- ORDERS
-- Clients 1-7 each have 1-2 orders; employee assignment varies
-- ============================================================
INSERT INTO orders (client_id, employee_id, order_date, price)
VALUES
    -- client 1 (Medelyn Wright) — 2 orders
    (1, 1, '2024-01-10 09:15:00', 54.94),   -- order 1
    (1, 3, '2024-03-22 14:30:00', 29.95),   -- order 2
    -- client 2 (Landon Phillips) — 2 orders
    (2, 2, '2024-02-05 11:00:00', 38.49),   -- order 3
    (2, NULL, '2024-04-18 16:45:00', 26.50),-- order 4 (not yet assigned)
    -- client 3 (Harmony Mason) — 1 order
    (3, 4, '2024-01-28 10:20:00', 64.00),   -- order 5
    -- client 4 (Archer Harper) — 2 orders
    (4, 5, '2024-02-14 13:00:00', 43.50),   -- order 6
    (4, 1, '2024-05-01 09:00:00', 32.00),   -- order 7
    -- client 5 (Kira Jacobs) — 1 order
    (5, 6, '2024-03-09 15:30:00', 33.98),   -- order 8
    -- client 6 (Maximus Kelly) — 1 order
    (6, NULL, '2024-04-25 12:00:00', 46.99),-- order 9 (pending)
    -- client 9 (Amina Clarke) — 1 order
    (9, 7, '2024-05-10 17:00:00', 55.50),   -- order 10
    -- client 10 (Leonid Sazonov) — 1 order
    (10, 2, '2024-05-15 08:45:00', 28.00);  -- order 11

-- ============================================================
-- BOOK ITEMS  (line items for each order above)
-- ============================================================
INSERT INTO book_items (order_id, book_id, quantity)
VALUES
    -- Order 1: The Hidden Treasure x1 + Echoes of Eternity x2
    (1,  1, 1),
    (1,  2, 2),
    -- Order 2: Whispers in the Shadows x1
    (2,  3, 1),
    -- Order 3: The Starlight Sonata x1 + Echoes of Eternity x1
    (3,  4, 1),
    (3,  2, 1),
    -- Order 4: Dancing with Shadows x1
    (4,  6, 1),
    -- Order 5: Voices in the Wind x2
    (5,  7, 2),
    -- Order 6: Silent Whispers x1 + Serenade of Souls x1
    (6,  9, 1),
    (6,  8, 1),
    -- Order 7: Voices in the Wind x1
    (7,  7, 1),
    -- Order 8: Serenade of Souls x1 + Whirlwind Romance x1
    (8,  8, 1),
    (8, 10, 1),
    -- Order 9: The Forgotten Kingdom x1 + Stars and Satellites x1
    (9, 11, 1),
    (9, 13, 1),
    -- Order 10: Midnight Protocol x1 + The Amber Compass x1
    (10, 12, 1),
    (10, 14, 1),
    -- Order 11: Midnight Protocol x1
    (11, 12, 1);

-- ============================================================
-- BASKETS  (books currently in clients' shopping baskets)
-- ============================================================
INSERT INTO baskets (client_id, book_id)
VALUES
    -- Medelyn Wright browsing fantasy & sci-fi
    (1, 11),  -- The Forgotten Kingdom
    (1, 19),  -- Junior Galaxy Rangers
    -- Landon Phillips eyeing some thrillers
    (2, 16),  -- Burning Tundra
    (2, 12),  -- Midnight Protocol
    -- Harmony Mason considering romance
    (3, 18),  -- Moonlit Waltz
    (3, 10),  -- Whirlwind Romance
    -- Archer Harper looking at historical fiction
    (4, 15),  -- Letters to Nowhere
    (4, 20),  -- Shadow Parliament
    -- Kira Jacobs interested in mystery
    (5, 17),  -- The Glass Menagerie Code
    -- Leonid Sazonov checking out adventure
    (10, 1),  -- The Hidden Treasure
    (10, 14); -- The Amber Compass