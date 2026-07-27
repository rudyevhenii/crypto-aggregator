INSERT INTO "exchange_pairs" ("id", "trading_pair", "exchange")
SELECT
    gen_random_uuid(),
    pair,
    exch
FROM (
    SELECT unnest(ARRAY[
        'BTC_USD', 'ETH_USD', 'SOL_USD', 'ADA_USD', 'XRP_USD',
        'DOT_USD', 'DOGE_USD', 'LINK_USD', 'LTC_USD', 'AVAX_USD'
    ]) AS pair
) AS pairs
CROSS JOIN (
    SELECT unnest(ARRAY[
        'BINANCE', 'COINBASE', 'KRAKEN'
    ]) AS exch
) AS exchanges
ON CONFLICT ("trading_pair", "exchange") DO NOTHING;
