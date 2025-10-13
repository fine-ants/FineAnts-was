ALTER TABLE fineAnts.stock_dividend
    ADD UNIQUE KEY uk_stock_dividend_ticker_symbol_record_date (ticker_symbol, record_date);
