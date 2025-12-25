# Test Data SQL Files

These SQL files contain test data for development and staging environments.

**WARNING: Do NOT run these in production!**

## Files

| File | Description | Dependencies |
|------|-------------|--------------|
| `01_test_members.sql` | Test users (일반/기업 회원) | Flyway migrations |
| `02_test_certificates.sql` | Test certificates (수료증) | 01_test_members.sql |
| `03_test_reviews.sql` | Test reviews (리뷰) | 02_test_certificates.sql |
| `04_test_banners.sql` | Test banners (배너) | Flyway migrations |
| `05_test_cart.sql` | Test cart items (장바구니) | 01_test_members.sql |

## Usage

Run after Flyway migrations have been applied:

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d swcampus

# Run in order
\i 01_test_members.sql
\i 02_test_certificates.sql
\i 03_test_reviews.sql
\i 04_test_banners.sql
\i 05_test_cart.sql
```

Or use the all-in-one script:

```bash
cat 01_test_members.sql 02_test_certificates.sql 03_test_reviews.sql 04_test_banners.sql 05_test_cart.sql | psql -h localhost -U postgres -d swcampus
```

## Test Accounts

| Email | Password | Role | Note |
|-------|----------|------|------|
| admin@swcampus.co.kr | admin123 | ADMIN | Created by Flyway V2 |
| user1@test.com | admin123 | USER | 김철수 |
| user2@test.com | admin123 | USER | 이영희 |
| baemin@woowa.com | admin123 | ORGANIZATION | 우아한형제들 담당자 |
| manager@multicampus.com | admin123 | ORGANIZATION | 멀티캠퍼스 담당자 |
