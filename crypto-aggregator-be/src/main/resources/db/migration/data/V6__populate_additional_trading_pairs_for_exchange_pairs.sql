INSERT INTO exchange_pairs (id, trading_pair, exchange)
SELECT
    gen_random_uuid(),
    pair,
    exch
FROM (
    SELECT unnest(ARRAY[
        'MATIC_USD', 'SHIB_USD', 'UNI_USD', 'ATOM_USD', 'TRX_USD', 'NEAR_USD',
        'ICP_USD', 'APT_USD', 'ARB_USD', 'OP_USD', 'XLM_USD', 'BCH_USD', 'ETC_USD',
        'FIL_USD', 'INJ_USD', 'LDO_USD', 'ALGO_USD', 'GRT_USD', 'SAND_USD', 'MANA_USD'
    ]) AS pair
) AS pairs
CROSS JOIN (
    SELECT unnest(ARRAY[
        'BINANCE', 'COINBASE', 'KRAKEN'
    ]) AS exch
) AS exchanges
ON CONFLICT (trading_pair, exchange) DO NOTHING;