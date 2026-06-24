-- WordClub 用户学习表（首次部署执行）
-- 在导入语料库后运行此脚本创建 SM-2 进度和收藏表

CREATE TABLE IF NOT EXISTS `user_word_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `word_id` BIGINT NOT NULL,
    `book_id` BIGINT DEFAULT NULL,
    `ease_factor` DOUBLE DEFAULT 2.5,
    `review_interval` INT DEFAULT 0,
    `repetitions` INT DEFAULT 0,
    `next_review_at` DATETIME(6) DEFAULT NULL,
    `status` VARCHAR(20) DEFAULT 'NEW',
    `created_at` DATETIME(6) DEFAULT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_favorites` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `word_id` BIGINT NOT NULL,
    `created_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE,
    `new_word_count` INT DEFAULT 50,
    `review_ratio` INT DEFAULT 1,
    `card_order` VARCHAR(20) DEFAULT 'random',
    `large_font` TINYINT(1) DEFAULT 0,
    `dark_mode` TINYINT(1) DEFAULT 0,
    `learning_mode` VARCHAR(20) DEFAULT 'first-sight',
    `exam_date` VARCHAR(20) DEFAULT '',
    `selected_book_id` BIGINT DEFAULT NULL,
    `created_at` DATETIME(6) DEFAULT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
