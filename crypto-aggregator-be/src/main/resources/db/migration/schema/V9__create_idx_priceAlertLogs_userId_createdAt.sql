CREATE INDEX IF NOT EXISTS idx_price_alert_logs_user_id_created_at ON "priceAlertLogs" ("userId", "createdAt" DESC);
