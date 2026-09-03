-- Seed ROLE_SELLER and ROLE_TRAINEE into roles table
INSERT INTO roles (id, description)
VALUES 
('ROLE_SELLER', 'Independent marketplace seller with product, inventory, order, and payout management capabilities'),
('ROLE_TRAINEE', 'Enrolled participant in cultivation training programs')
ON CONFLICT (id) DO NOTHING;

-- Seed default demo seller and trainee user accounts if needed
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status)
VALUES 
('seller-1', 'seller@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Marketplace', 'Seller', 'ROLE_SELLER', 'ACTIVE'),
('trainee-1', 'trainee@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Ramesh', 'Trainee', 'ROLE_TRAINEE', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES 
('seller-1', 'ROLE_SELLER'),
('trainee-1', 'ROLE_TRAINEE')
ON CONFLICT (user_id, role_id) DO NOTHING;
