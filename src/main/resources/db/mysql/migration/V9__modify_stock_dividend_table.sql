-- 1. line_idx 컬럼 추가
ALTER TABLE stock_dividend
    ADD COLUMN line_idx INT NOT NULL DEFAULT 0;

-- 2. ticker_symbol별 순번 계산 후 line_idx 채우기
WITH ranked AS (SELECT ticker_symbol,
                       record_date,
                       ROW_NUMBER() OVER (PARTITION BY ticker_symbol ORDER BY record_date) - 1 AS rn
                FROM stock_dividend)
UPDATE stock_dividend sd
    JOIN ranked r
    ON sd.ticker_symbol = r.ticker_symbol
        AND sd.record_date = r.record_date
SET sd.line_idx = r.rn;
