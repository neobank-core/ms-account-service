CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    iban VARCHAR(34) UNIQUE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE account_limits (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    daily_limit NUMERIC(19,2) NOT NULL,
    monthly_limit NUMERIC(19,2) NOT NULL
);