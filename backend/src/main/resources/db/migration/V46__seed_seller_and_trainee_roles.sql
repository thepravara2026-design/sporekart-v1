-- Seed ROLE_SELLER and ROLE_TRAINEE into roles table
INSERT INTO roles (id, description)
SELECT 'ROLE_SELLER', 'Independent marketplace seller with product, inventory, order, and payout management capabilities'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id = 'ROLE_SELLER');

INSERT INTO roles (id, description)
SELECT 'ROLE_TRAINEE', 'Enrolled participant in cultivation training programs'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id = 'ROLE_TRAINEE');

-- Seed default demo seller and trainee user accounts if needed
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status)
SELECT 'seller-1', 'seller@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Marketplace', 'Seller', 'ROLE_SELLER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 'seller-1');

INSERT INTO users (id, email, password_hash, first_name, last_name, role, status)
SELECT 'trainee-1', 'trainee@sporekart.com', '$2a$10$e4gRkGgM1L9X2V3Y4Z5W6u7V8W9X0Y1Z2A3B4C5D6E7F8G9H0I1J2', 'Ramesh', 'Trainee', 'ROLE_TRAINEE', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 'trainee-1');

INSERT INTO user_roles (user_id, role_id)
SELECT 'seller-1', 'ROLE_SELLER'
WHERE NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = 'seller-1' AND role_id = 'ROLE_SELLER');

INSERT INTO user_roles (user_id, role_id)
SELECT 'trainee-1', 'ROLE_TRAINEE'
WHERE NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = 'trainee-1' AND role_id = 'ROLE_TRAINEE');
