CREATE TABLE recommendation_feedbacks (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_id UUID    NOT NULL,
    is_positive       BOOLEAN NOT NULL,
    comment           TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_rec_feedback_rec FOREIGN KEY (recommendation_id)
        REFERENCES recommendations(id) ON DELETE CASCADE
);

CREATE INDEX idx_rec_feedback_recommendation_id ON recommendation_feedbacks(recommendation_id);
CREATE INDEX idx_rec_feedback_created_at ON recommendation_feedbacks(created_at DESC);
