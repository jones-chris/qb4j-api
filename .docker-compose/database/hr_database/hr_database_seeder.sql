USE qb4j;

CREATE TABLE level (
    id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    level_num INTEGER NOT NULL UNIQUE,
    min_salary FLOAT NOT NULL,  -- Annual salary
    max_salary FLOAT NOT NULL  -- Annual salary
);

INSERT INTO level (level_num, min_salary, max_salary) VALUES (1, 30000, 39999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (2, 40000, 42999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (3, 43000, 55999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (4, 56000, 68999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (5, 69000, 80999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (6, 81000, 94999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (7, 95000, 109999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (8, 110000, 119999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (9, 120000, 133999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (10, 134000, 144999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (11, 145000, 159999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (12, 160000, 184999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (13, 185000, 200999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (14, 201000, 217999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (15, 218000, 232999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (16, 233000, 244999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (17, 245000, 264999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (18, 265000, 280999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (19, 281000, 294999);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (20, 295000, 300000);
INSERT INTO level (level_num, min_salary, max_salary) VALUES (21, 300001, 400000);

CREATE TABLE role (
    id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(30) NOT NULL UNIQUE,
    level_id INTEGER,

    FOREIGN KEY (level_id) REFERENCES level(id)
);

INSERT INTO role (name, level_id) VALUES ('Administrator', 2);
INSERT INTO role (name, level_id) VALUES ('Senior Administrator', 3);
INSERT INTO role (name, level_id) VALUES ('Truck Driver', 3);
INSERT INTO role (name, level_id) VALUES ('Senior Truck Driver', 4);
INSERT INTO role (name, level_id) VALUES ('Accountant', 3);
INSERT INTO role (name, level_id) VALUES ('Senior Accountant', 4);
INSERT INTO role (name, level_id) VALUES ('Analyst', 4);
INSERT INTO role (name, level_id) VALUES ('Senior Analyst', 5);
INSERT INTO role (name, level_id) VALUES ('Engineer', 5);
INSERT INTO role (name, level_id) VALUES ('Senior Engineer', 6);
INSERT INTO role (name, level_id) VALUES ('Consultant', 7);
INSERT INTO role (name, level_id) VALUES ('Senior Consultant', 8);
INSERT INTO role (name, level_id) VALUES ('Advisor', 7);
INSERT INTO role (name, level_id) VALUES ('Senior Advisor', 8);
INSERT INTO role (name, level_id) VALUES ('Team Leader', 9);
INSERT INTO role (name, level_id) VALUES ('Senior Team Leader', 10);
INSERT INTO role (name, level_id) VALUES ('Director', 11);
INSERT INTO role (name, level_id) VALUES ('Senior Director', 12);
INSERT INTO role (name, level_id) VALUES ('Vice President', 13);
INSERT INTO role (name, level_id) VALUES ('Senior Vice President', 14);
INSERT INTO role (name, level_id) VALUES ('Officer', 15);
INSERT INTO role (name, level_id) VALUES ('Senior Officer', 16);
INSERT INTO role (name, level_id) VALUES ('Chief Officer', 17);
INSERT INTO role (name, level_id) VALUES ('President', 18);
INSERT INTO role (name, level_id) VALUES ('Emporer', 19);  -- One day we'll have the title of Emporer in corporate America :P !
INSERT INTO role (name, level_id) VALUES ('Senior Emperor', 20);
INSERT INTO role (name, level_id) VALUES ('Grand Emporer', 21);

CREATE TABLE team (
    id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    short_name VARCHAR(4) NOT NULL UNIQUE,
    long_name VARCHAR(40) NOT NULL UNIQUE
);

INSERT INTO team (short_name, long_name) VALUES ('TXMA', 'Textile Manufacturing');
INSERT INTO team (short_name, long_name) VALUES ('PLMA', 'Plastics Manufacturing');
INSERT INTO team (short_name, long_name) VALUES ('MTMA', 'Metallurgy Manufacturing');
INSERT INTO team (short_name, long_name) VALUES ('MANU', 'Manufacturing');
INSERT INTO team (short_name, long_name) VALUES ('LMAR', 'Local Marketing');
INSERT INTO team (short_name, long_name) VALUES ('RMAR', 'Regional Marketing');
INSERT INTO team (short_name, long_name) VALUES ('NMAR', 'National Marketing');
INSERT INTO team (short_name, long_name) VALUES ('IMAR', 'International Marketing');
INSERT INTO team (short_name, long_name) VALUES ('SALE', 'Sales');
INSERT INTO team (short_name, long_name) VALUES ('MARK', 'Marketing');
INSERT INTO team (short_name, long_name) VALUES ('HD', 'Help Desk');
INSERT INTO team (short_name, long_name) VALUES ('FS', 'Field Support');
INSERT INTO team (short_name, long_name) VALUES ('CS', 'Customer Support');
INSERT INTO team (short_name, long_name) VALUES ('MECH', 'Mechanical Engineering');
INSERT INTO team (short_name, long_name) VALUES ('SFTW', 'Software Engineering');
INSERT INTO team (short_name, long_name) VALUES ('EN', 'Engineering');
INSERT INTO team (short_name, long_name) VALUES ('PUR', 'Purchasing');
INSERT INTO team (short_name, long_name) VALUES ('PRJE', 'Project Estimation');
INSERT INTO team (short_name, long_name) VALUES ('ACCT', 'Accounting & Tax');
INSERT INTO team (short_name, long_name) VALUES ('BUDG', 'Budgeting');
INSERT INTO team (short_name, long_name) VALUES ('FIN', 'Finance');
INSERT INTO team (short_name, long_name) VALUES ('EXCO', 'Executive Committee');
INSERT INTO team (short_name, long_name) VALUES ('BOD', 'Board of Directors');

-- The team hierarchy.
-- A team can only map to 1 parent, technically this prevents this from being a many-to-many mapping table,
-- but it provides flexibility if the data model relationship changes in the future.
CREATE TABLE team_mapping (
    child_id INTEGER PRIMARY KEY NOT NULL,
    parent_id INTEGER NOT NULL,

    UNIQUE(child_id, parent_id),
    FOREIGN KEY (child_id) REFERENCES team(id),
    FOREIGN KEY (parent_id) REFERENCES team(id)
);

INSERT INTO team_mapping (child_id, parent_id) VALUES (1, 4);
INSERT INTO team_mapping (child_id, parent_id) VALUES (2, 4);
INSERT INTO team_mapping (child_id, parent_id) VALUES (3, 4);
INSERT INTO team_mapping (child_id, parent_id) VALUES (4, 22);
INSERT INTO team_mapping (child_id, parent_id) VALUES (5, 10);
INSERT INTO team_mapping (child_id, parent_id) VALUES (6, 10);
INSERT INTO team_mapping (child_id, parent_id) VALUES (7, 10);
INSERT INTO team_mapping (child_id, parent_id) VALUES (8, 10);
INSERT INTO team_mapping (child_id, parent_id) VALUES (9, 10);
INSERT INTO team_mapping (child_id, parent_id) VALUES (10, 22);
INSERT INTO team_mapping (child_id, parent_id) VALUES (11, 13);
INSERT INTO team_mapping (child_id, parent_id) VALUES (12, 13);
INSERT INTO team_mapping (child_id, parent_id) VALUES (13, 22);
INSERT INTO team_mapping (child_id, parent_id) VALUES (14, 16);
INSERT INTO team_mapping (child_id, parent_id) VALUES (15, 16);
INSERT INTO team_mapping (child_id, parent_id) VALUES (16, 22);
INSERT INTO team_mapping (child_id, parent_id) VALUES (17, 21);
INSERT INTO team_mapping (child_id, parent_id) VALUES (18, 21);
INSERT INTO team_mapping (child_id, parent_id) VALUES (19, 21);
INSERT INTO team_mapping (child_id, parent_id) VALUES (20, 21);
INSERT INTO team_mapping (child_id, parent_id) VALUES (21, 22);
INSERT INTO team_mapping (child_id, parent_id) VALUES (22, 23);

CREATE TABLE employee (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    guid VARCHAR(50) NOT NULL UNIQUE DEFAULT (uuid()),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    team_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,

    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Tai', 'Barlow', 1, 2);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Stan', 'Prince', 1, 2);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Jannah', 'Chase', 1, 2);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Jackson', 'Gamble', 2, 3);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Alvin', 'Burke', 2, 4);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Chay', 'Witt', 2, 3);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Ernie', 'Clifford', 3, 4);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Giorgia', 'Salaz', 3, 3);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Alena', 'Swanson', 3, 2);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Jeffrey', 'Glover', 4, 17);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Antonina', 'Stark', 4, 15);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Wyatt', 'Blankenship', 5, 8);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Arif', 'Bird', 5, 5);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Emelie', 'Parry', 6, 5);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Johnny', 'O''Moore', 6, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Yassin', 'Lopez', 7, 5);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Malika', 'Marsden', 7, 4);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Lyla-Rose', 'Harwood', 8, 5);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Eilidh', 'McKay', 8, 5);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Sonya', 'McCullough', 9, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Emme', 'Leon', 9, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Patricia', 'McDougall', 10, 16);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Rebekah', 'Allman', 10, 17);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Ernie', 'Marshall', 11, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Millie', 'Johns', 11, 8);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Saba', 'Kennedy', 12, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Tamara', 'Paine', 12, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Ishaan', 'Moody', 13, 16);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Shivam', 'Sargent', 13, 18);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Kobi', 'Millington', 14, 9);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Jasleen', 'Carson', 14, 10);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Myla', 'Ballard', 15, 9);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Tillie', 'Hampton', 15, 10);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Elora', 'Armstrong', 16, 16);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Nile', 'Odom', 16, 18);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Dylon', 'Villarreal', 17, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Eliott', 'Brett', 18, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Joss', 'Rhodes', 19, 7);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Izabel', 'Manning', 20, 8);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Amrit', 'Webber', 21, 17);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Margaret', 'Driscoll', 22, 19);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Mica', 'Millar', 22, 20);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Noah', 'Jennings', 22, 21);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Bo', 'Cardenas', 23, 22);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Yousef', 'Wooley', 23, 25);
INSERT INTO employee (first_name, last_name, team_id, role_id) VALUES ('Montague', 'Townsend', 23, 27);

-- Technically, `salary_amount` could have been a column in the `employee` table, but I thought putting salaries
-- in it's own separate table, `salary`, made it easier to restrict access to rather than restrict access to a column
-- of a table (this is all hypothetical obviously, since this is a mock database!).
CREATE TABLE salary (
    employee_id INTEGER NOT NULL UNIQUE,
    amount INTEGER NOT NULL, -- The annual salary.

    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

INSERT INTO salary (employee_id, amount) VALUES (1, 40000);
INSERT INTO salary (employee_id, amount) VALUES (2, 41000);
INSERT INTO salary (employee_id, amount) VALUES (3, 39000);
INSERT INTO salary (employee_id, amount) VALUES (4, 45000);
INSERT INTO salary (employee_id, amount) VALUES (5, 60000);
INSERT INTO salary (employee_id, amount) VALUES (6, 60000);
INSERT INTO salary (employee_id, amount) VALUES (7, 50000);
INSERT INTO salary (employee_id, amount) VALUES (8, 45000);
INSERT INTO salary (employee_id, amount) VALUES (9, 39000);
INSERT INTO salary (employee_id, amount) VALUES (10, 250000);
INSERT INTO salary (employee_id, amount) VALUES (11, 230000);
INSERT INTO salary (employee_id, amount) VALUES (12, 115000);
INSERT INTO salary (employee_id, amount) VALUES (13, 70000);
INSERT INTO salary (employee_id, amount) VALUES (14, 71000);
INSERT INTO salary (employee_id, amount) VALUES (15, 96000);
INSERT INTO salary (employee_id, amount) VALUES (16, 75000);
INSERT INTO salary (employee_id, amount) VALUES (17, 57000);
INSERT INTO salary (employee_id, amount) VALUES (18, 78000);
INSERT INTO salary (employee_id, amount) VALUES (19, 74000);
INSERT INTO salary (employee_id, amount) VALUES (20, 105000);
INSERT INTO salary (employee_id, amount) VALUES (21, 106000);
INSERT INTO salary (employee_id, amount) VALUES (22, 235000);
INSERT INTO salary (employee_id, amount) VALUES (23, 250000);
INSERT INTO salary (employee_id, amount) VALUES (24, 100000);
INSERT INTO salary (employee_id, amount) VALUES (25, 112000);
INSERT INTO salary (employee_id, amount) VALUES (26, 108000);
INSERT INTO salary (employee_id, amount) VALUES (27, 104000);
INSERT INTO salary (employee_id, amount) VALUES (28, 243000);
INSERT INTO salary (employee_id, amount) VALUES (29, 275000);
INSERT INTO salary (employee_id, amount) VALUES (30, 122000);
INSERT INTO salary (employee_id, amount) VALUES (31, 140000);
INSERT INTO salary (employee_id, amount) VALUES (32, 121000);
INSERT INTO salary (employee_id, amount) VALUES (33, 143000);
INSERT INTO salary (employee_id, amount) VALUES (34, 241000);
INSERT INTO salary (employee_id, amount) VALUES (35, 280000);
INSERT INTO salary (employee_id, amount) VALUES (36, 101000);
INSERT INTO salary (employee_id, amount) VALUES (37, 104000);
INSERT INTO salary (employee_id, amount) VALUES (38, 103000);
INSERT INTO salary (employee_id, amount) VALUES (39, 118000);
INSERT INTO salary (employee_id, amount) VALUES (40, 260000);
INSERT INTO salary (employee_id, amount) VALUES (41, 290000);
INSERT INTO salary (employee_id, amount) VALUES (42, 295000);
INSERT INTO salary (employee_id, amount) VALUES (43, 303000);
INSERT INTO salary (employee_id, amount) VALUES (44, 305000);
INSERT INTO salary (employee_id, amount) VALUES (45, 304000);
INSERT INTO salary (employee_id, amount) VALUES (46, 315000);

-- Employee hierarchy.
CREATE TABLE employee_mapping (
    child_id INTEGER PRIMARY KEY NOT NULL,
    supervisor_id INTEGER NOT NULL,

    UNIQUE (child_id, supervisor_id),
    FOREIGN KEY (child_id) REFERENCES employee (id),
    FOREIGN KEY (supervisor_id) REFERENCES employee(id)
);

-- Manufacturing
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (1, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (2, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (3, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (4, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (5, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (6, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (7, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (8, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (9, 11);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (10, 41);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (11, 10);

-- Marketing
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (12, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (13, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (14, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (15, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (16, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (17, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (18, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (19, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (20, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (21, 22);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (22, 23);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (23, 42);

-- Customer Support
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (24, 28);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (25, 28);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (26, 28);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (27, 28);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (28, 29);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (29, 43);

-- Engineering
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (30, 34);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (31, 34);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (32, 34);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (33, 34);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (34, 35);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (35, 43);

INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (36, 40);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (37, 40);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (38, 40);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (39, 40);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (40, 41);

-- Executive Committee
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (41, 44);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (42, 45);
INSERT INTO employee_mapping (child_id, supervisor_id) VALUES (43, 46);

COMMIT;
