CREATE TABLE transactions (
    id                  BIGSERIAL PRIMARY KEY,
    transaction_ref     VARCHAR(50)  NOT NULL UNIQUE,
    sender_id           BIGINT       NOT NULL,
    receiver_id         BIGINT       NOT NULL,
    amount              DECIMAL(19,4) NOT NULL,
    currency            VARCHAR(3)   NOT NULL DEFAULT 'USD',
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    transaction_type    VARCHAR(20)  NOT NULL,
    description         VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_ref ON transactions(transaction_ref);
CREATE INDEX idx_transactions_sender ON transactions(sender_id);
CREATE INDEX idx_transactions_receiver ON transactions(receiver_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created ON transactions(created_at);
