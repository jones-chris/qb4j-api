-- Creating the customers schema and its tables.

CREATE SCHEMA IF NOT EXISTS customers;

CREATE TABLE customers.customer (
    id SERIAL,
    name VARCHAR(50) NOT NULL,
    address_1 VARCHAR(100) NOT NULL,
    address_2 VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(50) NOT NULL,
    country_code VARCHAR(5) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    credit_card_number VARCHAR(40),
    phone_number CHAR(11),

    PRIMARY KEY(id)
);

INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Finley Jefferson', '1234 I Declare A Thumb War', 'Apt 42', 'Jalepeno', 'Imagination Land', 'US', '97124', 'xxxx-xxxx-xxxx-4242', '12025550171');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Aron Mcfarland', '52 Andrews Road', '', 'Bakersfield', 'California', 'US', '13402', 'xxxx-xxxx-xxxx-2412', '12025550188');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Marcos Lopez', '52 Wharf Hills', '', 'Norfolk', 'Virginia', 'US', '60704', 'xxxx-xxxx-xxxx-9902', '15185550140');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Lucca Arnold', '242 Acorn Parkway', '', 'Fort Worth', 'Texas', 'US', '11421', 'xxxx-xxxx-xxxx-4290', '15185550141');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Korey Sullivan', '980 Lewis Edge', '', 'Baltimore', 'Maryland', 'US', '28723', 'xxxx-xxxx-xxxx-2349', '14105550199');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Taine Galindo', '520 Fernhill Terrace', '', 'Charlotte', 'North Carolina', 'US', '42902', 'xxxx-xxxx-xxxx-4894', '14105550179');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Muneeb Landry', '58098 Little Ridgeway', '', 'Stockton', 'California', 'US', '74208', 'xxxx-xxxx-xxxx-4256', '14105550103');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Ayden Head', '892 Groombridge', '', 'Durham', 'North Carolina', 'US', '98742', 'xxxx-xxxx-xxxx-4240', '14105550198');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Alayna Pham', '52 Cliffe Banks', '', 'Philadelphia', 'Pennsylvania', 'US', '98724', 'xxxx-xxxx-xxxx-4291', '14105550104');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Leona Bains', '4 Franklin Grove', 'Apt 80', 'Plano', 'Texas', 'US', '78342', 'xxxx-xxxx-xxxx-0942', '14105550169');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Amelie Haworth', '522 Wheatley Sidings', '', 'Oakland', 'California', 'US', '42408', 'xxxx-xxxx-xxxx-5802', '14045550154');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Tori Dodson', '6789 Grimshaw Road', '', 'Atlanta', 'Georgia', 'US', '74729', 'xxxx-xxxx-xxxx-8902', '14045550136');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('US Central Intelligence Agency', '1 Stanhope Mead', 'Suite 4', 'Washington DC', 'Washington DC', 'US', '98724', 'xxxx-xxxx-xxxx-8209', '14045550144');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Jones and Co', '234 Latcham Court', '', 'Arlington', 'Virginia', 'US', '79724', 'xxxx-xxxx-xxxx-7520', '14045550176');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Martin Unlimited', '724 Campion Newydd', '', 'Buffalo', 'New York', 'US', '97472', 'xxxx-xxxx-xxxx-4821', '14045550137');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Henos Industries', '80 Leyburn Meadows', 'Suite 24', 'Nashville-Davidson', 'Tennessee', 'US', '97742', 'xxxx-xxxx-xxxx-9090', '14045550160');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Jones, Henos And Martin, Associates', '23 County Maltings', '', 'Oklahoma City', 'Oklahoma', 'US', '21414', 'xxxx-xxxx-xxxx-4029', '12255550136');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Hagad of Manchester', '864 Radcliffe Copse', '', 'Chesapeake', 'Virginia', 'US', '80234', 'xxxx-xxxx-xxxx-2892', '12255550172');
INSERT INTO customers.customer (name, address_1, address_2, city, state_province, country_code, postal_code, credit_card_number, phone_number) VALUES ('Pearl and Associates', '8253 Thackhall Street', 'Suite 100', 'Glendale', 'Arizona', 'US', '23400', 'xxxx-xxxx-xxxx-8921', '12255550188');

-- Many-to-many relationship.  Customers can have many account representatives and account representatives can have
-- many customers.
CREATE TABLE customers.customer_account_representative (
    employee_id INTEGER NOT NULL,  -- The employee id from the hr_database.employee table.
    customer_id INTEGER NOT NULL,

    UNIQUE(employee_id, customer_id),
    FOREIGN KEY(customer_id) REFERENCES customers.customer(id)
);

-- Creating the vendors schema and its tables.

CREATE SCHEMA IF NOT EXISTS vendors;

CREATE TABLE vendors.vendor (
    id SERIAL,
    name VARCHAR(50) NOT NULL,
    address_1 VARCHAR(100) NOT NULL,
    address_2 VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(50) NOT NULL,
    country_code VARCHAR(5) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    tax_id VARCHAR(40) NOT NULL,
    phone_number CHAR(11),

    PRIMARY KEY(id)
);

INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Reveo', '574 Selby Lane', '', 'Waterloo', 'Iowa', 'US', '52098', '991-78-8867', '12025550142');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Leezu', '7484 East Central Road', '', 'Brighton', 'Massachusetts', 'US', '13402', '927-74-7146', '12025550188');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Transescent', '246 Ashley Ave', '', 'Nottingham', 'Maryland', 'US', '60704', '979-84-5137', '15185550121');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Bioloo', '7378 Hilldale Road', '', 'Royal Oak', 'Michigan', 'US', '11421', '900-90-2437', '15185550141');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Fortosis', '474 Green Hill Street', '', 'Saint Cloud', 'Minnesota', 'US', '28723', '920-94-0167', '14105550199');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Skyore', '6 Fairview St.', '', 'Mount Prospect', 'Illinois', 'US', '25253', '932-90-2967', '14105550179');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Mistiva', '625 Fairway Rd', '', 'New Philadelphia', 'Ohio', 'US', '74208', '935-78-1491', '14105550103');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Cofy', '824 Depot Ave', '', 'Ashland', 'Ohio', 'US', '98742', '986-74-8080', '14105550198');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Syosis', '20 Pheasant St', '', 'Marshalltown', 'Iowa', 'US', '98724', '998-95-8426', '14105550142');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Infrape', '580 Henry Smith Rd', '', 'Clayton', 'North Carolina', 'US', '78342', '922-76-1640', '14105550169');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Haworth Real Estate', '2082 South Summerhouse St', '', 'Sugar Land', 'Texas', 'US', '42408', '911-71-5354', '14045550154');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Daza Services', '24 Richardson Drive', '', 'Petersburg', 'Virginia', 'US', '98740', '922-92-3026', '14045550136');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('US Central Intelligence Agency', '142 Willow Drive', '', 'Glendale', 'Arizona', 'US', '98724', '964-92-4905', '14045550144');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Summit ', '90 South Willow Ave', '', 'Willoughby', 'Ohio', 'US', '79724', '922-95-7131', '14045550176');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Henderson and Co', '992 Elmwood Drive', '', 'Mobile', 'Alabama', 'US', '97472', '952-95-2430', '14045550142');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Jackson Industries', '972 Lakeview Drive', 'Suite 24', 'Bergenfield', 'New Jersey', 'US', '97742', '970-94-3707', '14045550160');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Matrix Manufacturing', '983 Pendergast Dr', '', 'Emporia', 'Kansas', 'US', '52423', '986-92-9711', '12255550136');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Willow', '987 Manor Station Court', '', 'Olney', 'Maryland', 'US', '80234', '990-95-2300', '12234550172');
INSERT INTO vendors.vendor (name, address_1, address_2, city, state_province, country_code, postal_code, tax_id, phone_number) VALUES ('Pearl and Associates', '84 High Point Blvd', 'Suite 100', 'Milwaukee', 'Wisconsin', 'US', '23400', '942-74-5053', '12255550188');

-- Creating the financials schema and its tables.

CREATE SCHEMA IF NOT EXISTS financials;

CREATE TABLE financials.account (
    id SERIAL,
    code CHAR(6) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL,

    PRIMARY KEY (id)
);

INSERT INTO financials.account(code, name, active) VALUES ('100000', 'Cash', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('100001', 'Cash on Hand', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('100002', 'Short Term Investments', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('100010', 'Accounts Receivable', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('100020', 'Inventory', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('100030', 'Vehicles & Equipment', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('200000', 'Accounts Payable', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('200010', 'Payroll Tax Liability', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('200011', 'Federal Payroll Tax Liability', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('200012', 'State Payroll Tax Liability', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('200020', 'Sales Tax Liability', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('300000', 'Owner''s Capital', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('300010', 'Owner''s Withdrawals', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('300020', 'Retained Earnings', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('400000', 'Sales', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('400010', 'Returns and Allowances', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('400020', 'Cost of Goods Sold', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500000', 'Marketing and Advertising', TRUE); --18
INSERT INTO financials.account(code, name, active) VALUES ('500001', 'Marking and Advertising - Radio', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500002', 'Marking and Advertising - Television', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500003', 'Marking and Advertising - Social Media', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500010', 'Office Supplies', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500020', 'Salaries and Wages', TRUE); --23
INSERT INTO financials.account(code, name, active) VALUES ('500021', 'Officer Salary', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500022', 'Staff Salary', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500023', 'Hourly Wages', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500030', 'Payroll Taxes', TRUE);  --27
INSERT INTO financials.account(code, name, active) VALUES ('500031', 'Federal Payroll Taxes', TRUE);
INSERT INTO financials.account(code, name, active) VALUES ('500032', 'State Payroll Taxes', TRUE);

CREATE TABLE financials.account_mapping (
    child_id INTEGER NOT NULL,
    parent_id INTEGER NOT NULL,

    PRIMARY KEY(child_id),  -- Define a primary key because the an account can only have 1 parent.
    UNIQUE(child_id, parent_id),
    FOREIGN KEY(child_id) REFERENCES financials.account(id),
    FOREIGN KEY(parent_id) REFERENCES financials.account(id)
);

INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (2, 1);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (3, 1);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (9, 8);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (10, 8);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (11, 8);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (19, 18);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (20, 18);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (21, 18);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (24, 23);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (25, 23);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (26, 23);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (28, 27);
INSERT INTO financials.account_mapping (child_id, parent_id) VALUES (29, 27);

CREATE TABLE financials.sale (
    id SERIAL,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    amount DECIMAL(13, 2) NOT NULL,
    date TIMESTAMP NOT NULL,
    account_id INTEGER NOT NULL,
    description VARCHAR(100),
    customer_id INTEGER NOT NULL,

    PRIMARY KEY(id),
    FOREIGN KEY(account_id) REFERENCES financials.account(id),
    FOREIGN KEY(customer_id) REFERENCES customers.customer(id)
);

INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (267119.79, '2020-03-29 15:33:38', 15, 'My first sale!!', 1);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (240763.27, '2019-10-22 20:38:16', 15, 'My second sale!!', 2);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (129850.79, '2019-12-02 10:05:12', 15, 'My third sale!!', 3);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (63889.99, '2019-06-28 01:42:41', 15, 'Oh my gosh my fourth sale!!', 4);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (114075.76, '2020-02-18 04:41:26', 15, NULL, 5);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (59348.9, '2019-07-09 00:03:42', 15, NULL, 6);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (331396.8, '2020-07-28 18:33:39', 15, NULL, 7);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (81034.86, '2020-06-28 02:27:09', 15, NULL, 8);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (146281.1, '2020-03-06 12:39:19', 15, NULL, 9);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (105658.79, '2019-06-18 10:18:30', 15, NULL, 10);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (382821.69, '2019-05-21 02:39:07', 15, NULL, 11);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (140902.86, '2019-01-28 18:14:37', 15, NULL, 12);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (181890.12, '2020-07-15 01:47:22', 15, NULL, 13);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (308917.43, '2019-08-14 16:55:35', 15, NULL, 14);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (232524.18, '2020-02-25 03:18:06', 15, NULL, 15);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (99609.09, '2019-12-05 05:53:29', 15, NULL, 16);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (95943.8, '2020-02-10 03:42:55', 15, NULL, 17);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (207319.04, '2020-02-04 04:17:58', 15, NULL, 18);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (110206.52, '2020-02-04 04:17:58', 15, NULL, 19);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (258212.03, '2020-02-04 04:17:58', 15, NULL, 1);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (345919.68, '2020-02-04 04:17:58', 15, NULL, 2);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (31354.17, '2020-02-04 04:17:58', 15, NULL, 3);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (324049.12, '2020-02-04 04:17:58', 15, NULL, 4);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (244753.44, '2020-02-04 04:17:58', 15, NULL, 5);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (253806.19, '2020-02-04 04:17:58', 15, NULL, 6);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (419947.07, '2020-02-04 04:17:58', 15, NULL, 7);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (53501.12, '2020-02-04 04:17:58', 15, NULL, 8);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (73707.17, '2020-02-04 04:17:58', 15, NULL, 9);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (333508.28, '2020-02-04 04:17:58', 15, NULL, 10);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (53682.64, '2019-08-26 20:22:14', 15, NULL, 11);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (242017.59, '2019-11-15 12:48:06', 15, NULL, 12);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (358108.94, '2019-11-15 12:48:06', 15, NULL, 13);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (350457.9, '2019-11-15 12:48:06', 15, NULL, 14);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (219772.36, '2019-11-15 12:48:06', 15, NULL, 15);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (338688.3, '2019-11-15 12:48:06', 15, NULL, 16);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (161321.21, '2019-11-15 12:48:06', 15, NULL, 17);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (187667.05, '2019-05-07 20:47:07', 15, NULL, 18);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (33821.6, '2019-05-07 20:47:07', 15, NULL, 19);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (218881.98, '2020-04-15 23:03:54', 15, NULL, 1);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (237392.5, '2020-04-15 23:03:54', 15, NULL, 2);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (241096.52, '2020-04-15 23:03:54', 15, NULL, 3);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (113626.51, '2020-04-15 23:03:54', 15, 'so…many…sales….', 4);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (222196.04, '2019-12-29 10:59:26', 15, NULL, 5);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (47816.81, '2019-06-13 22:35:01', 15, NULL, 6);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (370409.62, '2020-04-08 17:46:50', 15, NULL, 7);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (52782.63, '2020-01-12 08:04:58', 15, NULL, 8);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (194971.39, '2020-08-04 14:02:15', 15, 'I''m so tired….can''t make another sale', 9);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (52690.57, '2019-12-13 01:46:00', 15, NULL, 10);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (20970.3, '2019-08-13 13:34:08', 15, NULL, 11);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (51820.63, '2019-03-21 07:23:40', 15, NULL, 12);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (176682.57, '2019-01-04 18:37:49', 15, 'I don''t want to sell anymore….I''m so busy now', 13);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (81853.94, '2019-02-08 13:07:34', 15, NULL, 14);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (320280.41, '2020-04-23 01:57:15', 15, NULL, 15);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (217392.73, '2019-11-27 18:06:21', 15, NULL, 16);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (389626.47, '2019-01-10 06:58:31', 15, NULL, 17);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (224576.58, '2019-08-05 21:00:39', 15, NULL, 18);
INSERT INTO financials.sale (amount, date, account_id, description, customer_id) VALUES (30438.04, '2019-05-10 13:13:21', 15, 'My last sale!  Now I can sleep again!', 19);

CREATE TABLE financials.expense (
    id SERIAL,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    amount DECIMAL(13, 2) NOT NULL,
    date TIMESTAMP NOT NULL,
    account_id INTEGER NOT NULL,
    description VARCHAR(100),
    vendor_id INTEGER NOT NULL,

    PRIMARY KEY(id),
    FOREIGN KEY(account_id) REFERENCES financials.account(id),
    FOREIGN KEY(vendor_id) REFERENCES vendors.vendor(id)
);

INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (4234.79, '2019-01-10 06:58:31', 18, 'My first expense!!', 1);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (42342.27, '2019-10-22 20:38:16', 19, 'My second expense!!', 2);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (552.79, '2019-12-02 10:05:12', 20, 'My third expense!!', 3);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (8974.99, '2019-06-28 01:42:41', 21, 'Oh my gosh my fourth expense!!', 4);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (424.76, '2020-02-18 04:41:26', 22, NULL, 5);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (9874.9, '2019-07-09 00:03:42', 23, NULL, 6);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (497.8, '2020-07-28 18:33:39', 24, NULL, 7);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (4.86, '2020-06-28 02:27:09', 25, NULL, 8);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2498.1, '2020-03-06 12:39:19', 26, NULL, 9);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (4298.79, '2019-06-18 10:18:30', 27, NULL, 10);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (974.69, '2020-02-03 07:19:29', 28, NULL, 11);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (97242.86, '2019-01-28 18:14:37', 29, NULL, 12);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (97402.12, '2020-07-15 01:47:22', 18, NULL, 13);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (9879.43, '2019-08-14 16:55:35', 19, NULL, 14);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (232.18, '2020-02-25 03:18:06', 20, NULL, 15);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (909.09, '2019-12-05 05:53:29', 21, NULL, 16);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (9543.8, '2020-02-10 03:42:55', 22, NULL, 17);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2319.04, '2020-01-14 17:44:45', 23, NULL, 18);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (1206.52, '2020-02-04 04:17:58', 24, NULL, 19);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (8212.03, '2020-02-04 04:17:58', 25, NULL, 1);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (3419.68, '2020-02-04 04:17:58', 26, NULL, 2);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (314.17, '2020-02-04 04:17:58', 27, NULL, 3);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (3049.12, '2020-02-04 04:17:58', 28, NULL, 4);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2753.44, '2020-02-04 04:17:58', 29, NULL, 5);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2506.19, '2020-01-14 17:44:45', 18, NULL, 6);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (4947.07, '2020-02-04 04:17:58', 19, NULL, 7);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (3501.12, '2020-02-04 04:17:58', 20, NULL, 8);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (707.17, '2020-02-04 04:17:58', 21, NULL, 9);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (508.28, '2020-02-04 04:17:58', 22, NULL, 10);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2.64, '2019-08-26 20:22:14', 23, NULL, 11);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2017.59, '2019-11-15 12:48:06', 24, NULL, 12);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (35808.94, '2019-11-15 12:48:06', 25, NULL, 13);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (3457.9, '2019-11-15 12:48:06', 26, NULL, 14);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (21772.36, '2019-11-15 12:48:06', 27, NULL, 15);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (8688.3, '2019-11-15 12:48:06', 28, NULL, 16);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (11321.21, '2019-11-15 12:48:06', 29, NULL, 17);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (1877.05, '2019-05-07 20:47:07', 18, NULL, 18);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (331.6, '2019-05-07 20:47:07', 19, NULL, 19);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (8881.98, '2020-04-15 23:03:54', 20, NULL, 1);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (7392.5, '2020-04-15 23:03:54', 21, NULL, 2);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (296.52, '2020-04-15 23:03:54', 22, NULL, 3);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (11626.51, '2020-04-15 23:03:54', 23, 'so…many…expenses….', 4);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2196.04, '2019-12-29 10:59:26', 24, NULL, 5);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (4786.81, '2019-06-13 22:35:01', 25, NULL, 6);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (37040.62, '2020-04-08 17:46:50', 26, NULL, 7);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (5272.63, '2020-01-12 08:04:58', 27, NULL, 8);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (1944971.39, '2020-08-04 14:02:15', 28, 'I''m so tired….can''t make another expense', 9);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (5260.57, '2019-12-13 01:46:00', 29, NULL, 10);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (270.3, '2019-08-13 13:34:08', 18, NULL, 11);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (5120.63, '2019-03-21 07:23:40', 19, NULL, 12);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (17682.57, '2019-01-04 18:37:49', 20, 'I don''t want to expense anymore….I''m so busy now', 13);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (8853.94, '2019-02-08 13:07:34', 21, NULL, 14);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (32280.41, '2020-04-23 01:57:15', 22, NULL, 15);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (2392.73, '2019-11-27 18:06:21', 23, NULL, 16);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (3626.47, '2019-01-10 06:58:31', 24, NULL, 17);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (22576.58, '2019-08-05 21:00:39', 25, NULL, 18);
INSERT INTO financials.expense (amount, date, account_id, description, vendor_id) VALUES (3438.04, '2019-05-10 13:13:21', 26, 'My last expense!  Now I can sleep again!', 19);

-- Creating the qb4j schema and query_templates table.

CREATE SCHEMA qb4j;

CREATE TABLE qb4j.query_templates(
   id SERIAL PRIMARY KEY,

   name VARCHAR(50) NOT NULL,
   version INTEGER NOT NULL,

   query_json JSONB NOT NULL,

   discoverable BOOLEAN NOT NULL DEFAULT FALSE,

   created_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   created_by VARCHAR(50) NOT NULL,
   last_updated_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   last_updated_by VARCHAR(50) NOT NULL,

   UNIQUE (name, version)
);
