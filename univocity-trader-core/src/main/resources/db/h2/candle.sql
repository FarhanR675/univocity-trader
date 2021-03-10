CREATE TABLE candle
(
	symbol     VARCHAR(32)     NOT NULL,
	open_time  BIGINT          NOT NULL,
	close_time BIGINT          NOT NULL,
	open       DECIMAL(20, 10) NOT NULL,
	high       DECIMAL(20, 10) NOT NULL,
	low        DECIMAL(20, 10) NOT NULL,
	close      DECIMAL(20, 10) NOT NULL,
	volume     DECIMAL(20, 10) NOT NULL,
    ts         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
;

ALTER TABLE candle ADD CONSTRAINT candle_symbol_time_uq UNIQUE (symbol, open_time, close_time);

CREATE INDEX candle_symbol_idx ON candle (symbol) USING HASH;
CREATE INDEX candle_symbol_open_idx ON candle (symbol, open_time) USING BTREE;
CREATE INDEX candle_symbol_close_idx ON candle (symbol, close_time) USING BTREE;
CREATE INDEX candle_open_close_idx ON candle (symbol, open_time, close_time) USING BTREE;
