INSERT INTO exchange_pairs (id, trading_pair, exchange)
SELECT
    gen_random_uuid(),
    pair,
    exch
FROM (
    SELECT unnest(ARRAY[
        'SHIB_USD', 'GRT_USD', 'UNI_USD', 'ATOM_USD', 'BCH_USD',
        'ETC_USD', 'XLM_USD', 'ALGO_USD', 'FIL_USD', 'ICP_USD',
        'OP_USD', 'ARB_USD', 'AAVE_USD', 'CRV_USD', 'SNX_USD',
        'COMP_USD', 'BAT_USD', 'ZRX_USD', 'YFI_USD', 'SUSHI_USD'
    ]) AS pair
) AS pairs
CROSS JOIN (
    SELECT unnest(ARRAY[
        'BINANCE', 'COINBASE', 'KRAKEN'
    ]) AS exch
) AS exchanges
ON CONFLICT (trading_pair, exchange) DO NOTHING;