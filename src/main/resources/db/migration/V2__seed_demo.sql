INSERT INTO transaction_ledger(account_id,amount,currency,booked_at,channel) VALUES
(1001,125.50,'USD', now()-interval '2 days','POS'),
(1001,-30.00,'USD', now()-interval '1 days','ATM'),
(1001,640.00,'USD', now()-interval '12 hours','TRANSFER'),
(1002,200.00,'EUR', now()-interval '1 days','POS');
