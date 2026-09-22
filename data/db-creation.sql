CREATE TABLE IF NOT EXISTS users (
   id SERIAL PRIMARY KEY,
   name VARCHAR(50) NOT NULL,
   email VARCHAR(100) NOT NULL,
   password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    balance DECIMAL(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    account_id INT NOT NULL REFERENCES accounts(id),
    amount DECIMAL(12, 2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

select * from users;

select * from accounts;

insert into users (name, email, password) values ('Edson Cruz', 'edson.l.cruz@gmail.com', '1234');

insert into accounts (name, balance, user_id) values ('Inter Checking Account', 0.00, 1);

commit;

drop table transactions;
drop table accounts;
drop table users;

drop sequence accounts_id_seq;
drop sequence transactions_id_seq;
drop sequence users_id_seq;