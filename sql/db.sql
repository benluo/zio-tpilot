create DATABASE reviewboard;
\c reviewboard;

CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    slug TEXT UNIQUE NOT NULL,
    name TEXT UNIQUE NOT NULL,
    url TEXT UNIQUE NOT NULL,
    location TEXT,
    country TEXT,
    industry TEXT,
    image TEXT,
    tags TEXT[]
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    management INT,
    culture INT,
    salary INT,
    benefits INT,
    would_recommend INT,
    review TEXT,
    created TIMESTAMP WITH TIME ZONE NOT NULL,
    updated TIMESTAMP WITH TIME ZONE NOT NULL
);