-- Blood Donor Management System Database Schema

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    city VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_city ON users (city);

-- Create donor_details table
CREATE TABLE IF NOT EXISTS donor_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    blood_group VARCHAR(10) NOT NULL,
    last_donation_date DATE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    total_donations INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_blood_group ON donor_details (blood_group);
CREATE INDEX IF NOT EXISTS idx_available ON donor_details (is_available);

-- Create blood_requests table
CREATE TABLE IF NOT EXISTS blood_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    blood_group VARCHAR(10) NOT NULL,
    hospital_name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    urgency_level VARCHAR(20) NOT NULL,
    units_required INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_blood_group ON blood_requests (blood_group);
CREATE INDEX IF NOT EXISTS idx_status ON blood_requests (status);
CREATE INDEX IF NOT EXISTS idx_location ON blood_requests (location);
CREATE INDEX IF NOT EXISTS idx_requester ON blood_requests (requester_id);

-- Create request_responses table
CREATE TABLE IF NOT EXISTS request_responses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blood_request_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    otp VARCHAR(6),
    otp_generated_at TIMESTAMP,
    otp_verified BOOLEAN DEFAULT FALSE,
    units_provided INT,
    response_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blood_request_id) REFERENCES blood_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (blood_request_id, donor_id)
);

CREATE INDEX IF NOT EXISTS idx_status ON request_responses (status);
CREATE INDEX IF NOT EXISTS idx_donor ON request_responses (donor_id);
CREATE INDEX IF NOT EXISTS idx_request ON request_responses (blood_request_id);

-- Create donation_history table
CREATE TABLE IF NOT EXISTS donation_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    request_response_id BIGINT,
    units_donated INT NOT NULL,
    blood_group VARCHAR(10) NOT NULL,
    hospital_name VARCHAR(255) NOT NULL,
    donation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (request_response_id) REFERENCES request_responses(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_donor ON donation_history (donor_id);
CREATE INDEX IF NOT EXISTS idx_donation_date ON donation_history (donation_date);

-- Create indexes for better query performance
-- Indexes already created above
CREATE INDEX idx_response_status ON request_responses(status);
CREATE INDEX idx_donation_donor ON donation_history(donor_id);
