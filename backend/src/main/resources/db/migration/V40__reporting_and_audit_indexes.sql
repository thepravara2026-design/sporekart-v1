-- Training 13: Reporting, Audit & Operational Controls Performance Indexes

CREATE INDEX IF NOT EXISTS idx_tr_enr_batch_status ON training_enrollments(batch_id, status);
CREATE INDEX IF NOT EXISTS idx_tr_enr_created_at ON training_enrollments(created_at);
CREATE INDEX IF NOT EXISTS idx_tr_pay_status ON training_enrollment_payments(status);
CREATE INDEX IF NOT EXISTS idx_tr_att_batch_status ON training_attendances(batch_id, status);
