USE cod;

-- Create Procedure
DELIMITER $$

CREATE PROCEDURE insert_crypto_transaction(
    IN p_date_utc TIMESTAMP,
    IN p_pair VARCHAR(255),
    IN p_side ENUM('BUY', 'SELL'),
    IN p_price DECIMAL(20, 10),
    IN p_executed_amount DECIMAL(20, 10),
    IN p_executed_currency VARCHAR(10),
    IN p_amount_amount DECIMAL(20, 10),
    IN p_amount_currency VARCHAR(10),
    IN p_fee_amount DECIMAL(20, 10),
    IN p_fee_currency VARCHAR(10)
)
BEGIN
    -- Check if a transaction with the same date_utc, pair, side, and price exists
    IF NOT EXISTS (
        SELECT 1
        FROM crypto_transaction
        WHERE date_utc = p_date_utc
          AND pair = p_pair
          AND side = p_side
          AND price = p_price
          AND executed_amount = p_executed_amount
          AND executed_currency = p_executed_currency
          AND amount_amount = p_amount_amount
          AND amount_currency = p_amount_currency
          AND fee_amount = p_fee_amount
          AND fee_currency = p_fee_currency
    ) THEN
        -- Insert the new transaction if no matching record is found
        INSERT INTO crypto_transaction (
            date_utc, pair, side, price, executed_amount, executed_currency,
            amount_amount, amount_currency, fee_amount, fee_currency
        )
        VALUES (
                   p_date_utc, p_pair, p_side, p_price, p_executed_amount, p_executed_currency,
                   p_amount_amount, p_amount_currency, p_fee_amount, p_fee_currency
               );
    END IF;
END$$

DELIMITER ;

-- ====================
-- Grant Permissions
-- ====================

-- Grant full privileges (read/write) to writer_user
GRANT EXECUTE ON PROCEDURE cod.insert_crypto_transaction TO 'writer_user'@'%';
GRANT ALL PRIVILEGES ON cod.crypto_transaction TO 'writer_user'@'%';

-- Grant read-only (SELECT) permissions to reader_user
GRANT SELECT ON cod.crypto_transaction TO 'reader_user'@'%';

-- Apply privileges
FLUSH PRIVILEGES;
