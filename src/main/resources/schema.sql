-- 사용자 테이블
CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password CHAR(60) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(user_id)
);

-- 계좌 테이블
CREATE TABLE accounts (
    account_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_number CHAR(15) NOT NULL UNIQUE,
    balance BIGINT NOT NULL DEFAULT 0,
    transfer_limit BIGINT,
    password CHAR(60) NOT NULL,
    type ENUM('PRIMARY','CHECKING','SAVINGS','MERCHANT') NOT NULL,
    status ENUM('ACTIVE','CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(account_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id)
);

-- 거래 테이블
CREATE TABLE transactions (
    transaction_id BIGINT NOT NULL AUTO_INCREMENT,
    sender_account_id BIGINT NOT NULL,
    recipient_account_id BIGINT NULL,
    amount BIGINT NOT NULL,
    balance BIGINT NOT NULL,
    status ENUM('COMPLETED','SCHEDULED','CANCELED') NOT NULL,
    type ENUM('TRANSFER_IN','TRANSFER_OUT','CARD_PAYMENT') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(transaction_id),
    FOREIGN KEY(sender_account_id) REFERENCES accounts(account_id),
    FOREIGN KEY(recipient_account_id) REFERENCES accounts(account_id)
);

-- 예약 송금 테이블 (선택적 1:1, PK = FK)
CREATE TABLE scheduled_transfers (
    transaction_id BIGINT NOT NULL,
    schedule_date DATETIME NOT NULL,
    memo VARCHAR(255) NULL,
    PRIMARY KEY(transaction_id),
    CONSTRAINT FK_ScheduledTransfer_Transaction
        FOREIGN KEY(transaction_id) REFERENCES transactions(transaction_id)
        ON DELETE CASCADE
);

-- 카드 결제 테이블 (선택적 1:1, PK = FK)
CREATE TABLE card_payments (
    transaction_id BIGINT NOT NULL,
    merchant_name VARCHAR(100) NOT NULL,
    PRIMARY KEY(transaction_id),
    CONSTRAINT FK_CardPayment_Transaction
        FOREIGN KEY(transaction_id) REFERENCES transactions(transaction_id)
        ON DELETE CASCADE
);