CREATE TABLE strategy_report (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    executive_summary TEXT NOT NULL,
    value_chain_impact TEXT NOT NULL,
    fde_delivery_analysis TEXT,
    pricing_model_analysis TEXT,
    agentic_ops_analysis TEXT,
    msp_opportunities_threats TEXT,
    top3_actions TEXT NOT NULL,
    source_signal_count INTEGER NOT NULL DEFAULT 0,
    generated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_strategy_report_generated_at ON strategy_report(generated_at DESC);
CREATE INDEX idx_strategy_report_period ON strategy_report(period_start, period_end);
