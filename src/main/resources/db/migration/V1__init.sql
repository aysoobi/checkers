CREATE TABLE profiles (
    user_id       VARCHAR(36) PRIMARY KEY,
    username      VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    city          VARCHAR(128),
    wins          INT NOT NULL DEFAULT 0,
    losses        INT NOT NULL DEFAULT 0,
    draws         INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE games (
    id            VARCHAR(36) PRIMARY KEY,
    user_id       VARCHAR(36) NOT NULL REFERENCES profiles (user_id) ON DELETE CASCADE,
    opponent_type VARCHAR(16) NOT NULL CHECK (opponent_type IN ('ai', 'human')),
    result        VARCHAR(8) CHECK (result IN ('win', 'lose', 'draw')),
    moves         VARCHAR(100000),
    played_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_games_user_id ON games (user_id);
CREATE INDEX idx_games_played_at ON games (played_at);
