INSERT INTO crawler_configs(
    id, name, enabled, base_url, proxy_enabled, proxy_name, requests_per_minute, burst,
    book_result_selector, book_rating_count_selector, book_rating_score_selector, search, title_space_separator)
VALUES
    (1, 'goodreads', true, 'https://www.goodreads.com', false, null, 30, 5,
     'a.bookTitle[itemprop=url]', 'div.RatingStatistics__meta', 'div.RatingStatistics__rating',
     '/search?q={formattedTitle}', '+'),

    (2, 'amazon-books', true, 'https://www.amazon.com', true, null, 30, 5,
     'div[data-cy=title-recipe] a.a-link-normal', '#acrCustomerReviewText',
     '#averageCustomerReviews span.a-size-small.a-color-base',
     '/s?k={formattedTitle}&i=stripbooks', '+'),

    (3, 'the-story-graph', true, 'https://app.thestorygraph.com', true, 'flaresolverr', 30, 5,
     'h3 > a[href^=/books/]', 'turbo-frame#community_reviews span.text-sm a.inverse-link',
     'span.average-star-rating', '/browse?search_term={formattedTitle}', '+');