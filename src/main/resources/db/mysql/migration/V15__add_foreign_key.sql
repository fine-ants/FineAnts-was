ALTER TABLE fineAnts.stock_dividend
    ADD CONSTRAINT fk_stock_dividend_ticker_symbol
        FOREIGN KEY (ticker_symbol)
            REFERENCES fineAnts.stock (ticker_symbol);
