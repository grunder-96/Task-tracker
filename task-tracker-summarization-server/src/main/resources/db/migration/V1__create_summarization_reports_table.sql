CREATE TABLE IF NOT EXISTS summarization_reports (
    request_id UUID PRIMARY KEY,
    report TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);