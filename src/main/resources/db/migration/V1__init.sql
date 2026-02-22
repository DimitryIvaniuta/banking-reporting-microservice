CREATE TABLE generated_report (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL,
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  report_type VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  transaction_count BIGINT NOT NULL,
  total_amount NUMERIC(19,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  failure_reason VARCHAR(512)
);
CREATE INDEX idx_generated_report_account_created_at ON generated_report(account_id, created_at DESC);
CREATE TABLE transaction_ledger (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  booked_at TIMESTAMPTZ NOT NULL,
  channel VARCHAR(32) NOT NULL
);
CREATE INDEX idx_tx_ledger_account_booked_at ON transaction_ledger(account_id, booked_at);
