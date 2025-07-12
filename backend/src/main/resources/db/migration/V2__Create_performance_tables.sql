-- Create performance_reviews table
CREATE TABLE performance_reviews (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    review_type VARCHAR(20) NOT NULL,
    review_period_start DATE NOT NULL,
    review_period_end DATE NOT NULL,
    due_date DATE,
    completed_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    overall_rating INTEGER,
    employee_self_assessment TEXT,
    manager_feedback TEXT,
    achievements TEXT,
    areas_for_improvement TEXT,
    goals_for_next_period TEXT,
    development_plan TEXT,
    training_recommendations TEXT,
    salary_adjustment_recommended BOOLEAN DEFAULT FALSE,
    promotion_recommended BOOLEAN DEFAULT FALSE,
    additional_comments TEXT,
    is_calibrated BOOLEAN DEFAULT FALSE,
    calibrated_by_id BIGINT,
    calibration_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_performance_review_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_performance_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES employees(id),
    CONSTRAINT fk_performance_review_calibrated_by FOREIGN KEY (calibrated_by_id) REFERENCES employees(id),
    CONSTRAINT chk_overall_rating CHECK (overall_rating >= 1 AND overall_rating <= 5)
);

-- Create performance_goals table
CREATE TABLE performance_goals (
    id BIGSERIAL PRIMARY KEY,
    performance_review_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    goal_type VARCHAR(30) NOT NULL,
    target_date DATE,
    completion_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    progress_percentage INTEGER DEFAULT 0,
    weight INTEGER,
    achievement_rating INTEGER,
    measurable_criteria TEXT,
    actual_result TEXT,
    manager_comments TEXT,
    employee_comments TEXT,
    is_carry_forward BOOLEAN DEFAULT FALSE,
    priority INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_performance_goal_review FOREIGN KEY (performance_review_id) REFERENCES performance_reviews(id) ON DELETE CASCADE,
    CONSTRAINT chk_progress_percentage CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    CONSTRAINT chk_weight CHECK (weight >= 0 AND weight <= 100),
    CONSTRAINT chk_achievement_rating CHECK (achievement_rating >= 1 AND achievement_rating <= 5),
    CONSTRAINT chk_priority CHECK (priority >= 1 AND priority <= 5)
);

-- Create performance_ratings table
CREATE TABLE performance_ratings (
    id BIGSERIAL PRIMARY KEY,
    performance_review_id BIGINT NOT NULL,
    rating_category VARCHAR(30) NOT NULL,
    rating INTEGER NOT NULL,
    weight INTEGER,
    comments TEXT,
    strengths TEXT,
    areas_for_improvement TEXT,
    examples TEXT,
    employee_self_rating INTEGER,
    manager_rating INTEGER,
    final_rating INTEGER,
    is_key_performance_indicator BOOLEAN DEFAULT FALSE,
    improvement_plan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_performance_rating_review FOREIGN KEY (performance_review_id) REFERENCES performance_reviews(id) ON DELETE CASCADE,
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT chk_weight_rating CHECK (weight >= 0 AND weight <= 100),
    CONSTRAINT chk_employee_self_rating CHECK (employee_self_rating >= 1 AND employee_self_rating <= 5),
    CONSTRAINT chk_manager_rating CHECK (manager_rating >= 1 AND manager_rating <= 5),
    CONSTRAINT chk_final_rating CHECK (final_rating >= 1 AND final_rating <= 5)
);

-- Create indexes for performance tables
CREATE INDEX idx_performance_reviews_employee ON performance_reviews(employee_id);
CREATE INDEX idx_performance_reviews_reviewer ON performance_reviews(reviewer_id);
CREATE INDEX idx_performance_reviews_status ON performance_reviews(status);
CREATE INDEX idx_performance_reviews_due_date ON performance_reviews(due_date);
CREATE INDEX idx_performance_reviews_review_type ON performance_reviews(review_type);
CREATE INDEX idx_performance_reviews_period ON performance_reviews(review_period_start, review_period_end);

CREATE INDEX idx_performance_goals_review ON performance_goals(performance_review_id);
CREATE INDEX idx_performance_goals_status ON performance_goals(status);
CREATE INDEX idx_performance_goals_target_date ON performance_goals(target_date);
CREATE INDEX idx_performance_goals_goal_type ON performance_goals(goal_type);

CREATE INDEX idx_performance_ratings_review ON performance_ratings(performance_review_id);
CREATE INDEX idx_performance_ratings_category ON performance_ratings(rating_category);
CREATE INDEX idx_performance_ratings_rating ON performance_ratings(rating);
CREATE INDEX idx_performance_ratings_kpi ON performance_ratings(is_key_performance_indicator);

-- Create trigger to update updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_performance_reviews_updated_at BEFORE UPDATE ON performance_reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_performance_goals_updated_at BEFORE UPDATE ON performance_goals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_performance_ratings_updated_at BEFORE UPDATE ON performance_ratings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Also add triggers for existing tables
CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leave_requests_updated_at BEFORE UPDATE ON leave_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_attendance_updated_at BEFORE UPDATE ON attendance
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();