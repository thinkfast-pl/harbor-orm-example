CREATE TABLE roles (
    id uuid PRIMARY KEY,
    name varchar(255) NOT NULL
);

CREATE TABLE roles_permissions (
    role_id uuid NOT NULL,
    permission ENUM('ROLE_ADD', 'ROLE_EDIT', 'ROLE_DELETE', 'USER_ADD', 'USER_EDIT', 'USER_DELETE') NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE users (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    email varchar(255) NOT NULL UNIQUE,
    type varchar(255) NOT NULL,
    active boolean NOT NULL,
    residential_address varchar(255),
    residential_town varchar(255),
    residential_building_no integer,
    residential_country varchar(255),
    contact_address varchar(255),
    contact_town varchar(255),
    contact_building_no integer,
    contact_country varchar(255)
);

CREATE TABLE users_roles (
    user_id bigint NOT NULL,
    role_id uuid NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE SEQUENCE customers_seq;

CREATE TABLE customers (
    id bigint PRIMARY KEY,
    email varchar(255) NOT NULL,
    create_date date NOT NULL,
    update_date date
);

CREATE TABLE customers_addresses (
    customer_id bigint NOT NULL,
    address varchar(255),
    town varchar(255),
    building_no integer,
    country varchar(255),
    FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE product_categories (
    id uuid PRIMARY KEY,
    name varchar(255) NOT NULL,
    active varchar(1) NOT NULL,
    version bigint NOT NULL
);

CREATE TABLE products (
    series varchar(255) NOT NULL,
    code varchar(255) NOT NULL,
    title varchar(255) NOT NULL,
    description longtext NOT NULL,
    photo longblob NOT NULL,
    active boolean NOT NULL,
    net_price numeric(19, 2) NOT NULL,
    gross_price numeric(19, 2) NOT NULL,
    PRIMARY KEY (series, code)
);

CREATE TABLE products_categories (
    product_series varchar(255) NOT NULL,
    product_code varchar(255) NOT NULL,
    product_category_id uuid NOT NULL,
    PRIMARY KEY (product_series, product_code, product_category_id),
    FOREIGN KEY (product_series, product_code) REFERENCES products (series, code),
    FOREIGN KEY (product_category_id) REFERENCES product_categories (id)
);

CREATE TABLE orders (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    customer_id bigint NOT NULL,
    status integer NOT NULL,
    creation_date datetime(6) NOT NULL,
    complete_date datetime(6),
    FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE orders_products (
    id uuid PRIMARY KEY,
    order_id bigint NOT NULL,
    series varchar(255) NOT NULL,
    code varchar(255) NOT NULL,
    title varchar(255) NOT NULL,
    net_price numeric(19, 2) NOT NULL,
    gross_price numeric(19, 2) NOT NULL,
    snapshot_date_time datetime(6) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE documents (
    id uuid PRIMARY KEY,
    body json NOT NULL,
    signature json NOT NULL
);

CREATE VIEW orders_reports AS
SELECT o.id                  AS order_id,
       o.customer_id         AS customer_id,
       c.email               AS customer_email,
       o.status              AS status,
       o.creation_date       AS creation_date,
       o.complete_date       AS complete_date,
       op.id                 AS product_id,
       op.series             AS product_series,
       op.code               AS product_code,
       op.title              AS product_title,
       op.net_price          AS product_net_price,
       op.gross_price        AS product_gross_price,
       op.snapshot_date_time AS snapshot_date_time
FROM orders o
JOIN customers c ON c.id = o.customer_id
JOIN orders_products op ON op.order_id = o.id;

CREATE PROCEDURE get_orders_by_product(IN p_series varchar(255), IN p_code varchar(255))
SELECT *
FROM orders_reports r
WHERE r.product_series = p_series
  AND r.product_code = p_code
ORDER BY r.creation_date DESC;
