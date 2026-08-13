# 데이터베이스 연결 가이드 (Database Integration Guide)

## 📋 Overview

프로젝트가 **Spring Data JPA**와 **MySQL**을 이용한 데이터베이스 통합을 완료했습니다. 테스트를 위해 **H2 인메모리 데이터베이스**도 설정되어 있습니다.

## ✅ 설정된 컴포넌트

### 1. JPA Entity 클래스
- **OrderEntity**: 주문 정보 (order_id, user_id, product_id, quantity, total_price, status, timestamps)
- **ProductEntity**: 상품 정보 (product_id, name, price, description, total_stock)
- **InventoryEntity**: 재고 관리 (product_id, available_quantity, reserved_quantity, sold_quantity)
- **PaymentEntity**: 결제 정보 (payment_id, order_id, amount, payment_method, status)

### 2. JPA Repository 인터페이스
```
repository/jpa/
├── OrderJpaRepository
├── ProductJpaRepository
├── InventoryJpaRepository
└── PaymentJpaRepository
```

### 3. 데이터베이스 설정
- **MySQL**: 프로덕션 환경
- **H2**: 테스트 환경 (인메모리)

## 🚀 시작하기

### 1단계: Docker Compose로 MySQL 시작

```bash
cd /workspaces/Java_Multithread
docker-compose up -d mysql
```

MySQL이 시작될 때까지 대기 (약 10-15초)

```bash
docker-compose logs mysql
```

### 2단계: 데이터베이스 확인

```bash
# MySQL 클라이언트 접속
docker exec -it flash-sale-mysql mysql -uroot -ppassword flashsale_db

# 테이블 확인
SHOW TABLES;
DESC orders;
DESC products;
DESC inventory;
DESC payments;

# 샘플 데이터 확인
SELECT * FROM products;
EXIT;
```

### 3단계: 애플리케이션 시작

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 4단계: API 테스트

#### 주문 생성
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "SKU-1001",
    "userId": "user-123",
    "quantity": 2
  }'
```

#### 응답 예시
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ACCEPTED",
  "productId": "SKU-1001",
  "quantity": 2,
  "remainingStock": 98,
  "userId": "user-123"
}
```

#### 재고 확인
```bash
curl http://localhost:8080/api/inventory
```

## 📊 데이터베이스 스키마

### Products 테이블
```sql
CREATE TABLE products (
    product_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500),
    total_stock INT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Orders 테이블
```sql
CREATE TABLE orders (
    order_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    status ENUM('QUEUED', 'PROCESSING', 'CONFIRMED', 'SHIPPED', 'CANCELLED'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE INDEX idx_user_id ON orders(user_id);
CREATE INDEX idx_product_id ON orders(product_id);
CREATE INDEX idx_status ON orders(status);
```

### Inventory 테이블
```sql
CREATE TABLE inventory (
    product_id VARCHAR(50) PRIMARY KEY,
    available_quantity INT NOT NULL,
    reserved_quantity INT NOT NULL,
    sold_quantity INT NOT NULL,
    last_updated TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
```

### Payments 테이블
```sql
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) UNIQUE NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status ENUM('SUCCESS', 'FAILED', 'PENDING'),
    processed_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
```

## 🧪 테스트 실행

### 모든 테스트 실행
```bash
./gradlew test
```

### 특정 테스트만 실행
```bash
# 데이터베이스 통합 테스트
./gradlew test --tests JpaRepositoryIntegrationTest

# 단순 데이터베이스 테스트
./gradlew test --tests SimpleDatabaseTest

# 플래시 세일 서비스 테스트
./gradlew test --tests FlashSaleServiceTest
```

### 테스트 결과 보기
```bash
# 리포트 열기
open build/reports/tests/test/index.html  # macOS
xdg-open build/reports/tests/test/index.html  # Linux
```

## 📈 테스트 현황

| 테스트 클래스 | 테스트 수 | 상태 |
|-------------|---------|------|
| FlashSaleServiceTest | 3 | ✅ PASS |
| InventoryTest | 5 | ✅ PASS |
| PaymentServiceTest | 3 | ✅ PASS |
| RepositoryTest | 3 | ✅ PASS |
| JpaRepositoryIntegrationTest | 8 | ✅ PASS |
| SimpleDatabaseTest | 3 | ✅ PASS |
| **총계** | **25** | ✅ **ALL PASS** |

## 💻 JPA 사용 예시

### Entity 저장
```java
@Autowired
private OrderJpaRepository orderRepository;

public void saveOrder() {
    OrderEntity order = new OrderEntity(
        "order-123",
        "user-1",
        "SKU-1001",
        2,
        new BigDecimal("2599.98")
    );
    orderRepository.save(order);
}
```

### 데이터 조회
```java
// ID로 조회
Optional<OrderEntity> order = orderRepository.findById("order-123");

// 사용자 ID로 모든 주문 조회
List<OrderEntity> userOrders = orderRepository.findByUserId("user-1");

// 상태별 주문 조회
List<OrderEntity> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.QUEUED);
```

### 데이터 업데이트
```java
OrderEntity order = orderRepository.findById("order-123").get();
order.setStatus(Order.OrderStatus.CONFIRMED);
orderRepository.save(order);
```

### 데이터 삭제
```java
orderRepository.deleteById("order-123");
orderRepository.deleteAll();
```

## 🔧 환경 설정 파일

### application.properties (프로덕션)
- MySQL 연결 설정
- Hibernate DDL-auto: update
- HikariCP 연결 풀: 10개

### application-test.properties (테스트)
- H2 인메모리 데이터베이스
- Hibernate DDL-auto: validate
- schema.sql 자동 로드

## 📁 프로젝트 구조

```
src/main/java/com/example/flashsale/
├── entity/                        # JPA Entity 클래스
│   ├── OrderEntity.java
│   ├── ProductEntity.java
│   ├── InventoryEntity.java
│   └── PaymentEntity.java
└── repository/
    └── jpa/                       # JPA Repository 인터페이스
        ├── OrderJpaRepository.java
        ├── ProductJpaRepository.java
        ├── InventoryJpaRepository.java
        └── PaymentJpaRepository.java

src/test/java/com/example/flashsale/
├── JpaRepositoryIntegrationTest.java    # 통합 테스트
└── SimpleDatabaseTest.java              # 단순 DB 테스트

src/test/resources/
└── schema.sql                           # 테스트용 DB 스키마
```

## ⚙️ application.properties 주요 설정

```properties
# MySQL 연결
spring.datasource.url=jdbc:mysql://localhost:3306/flashsale_db
spring.datasource.username=root
spring.datasource.password=password

# 연결 풀 설정
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000

# JPA/Hibernate 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

## 🐛 문제 해결

### MySQL 연결 실패
```bash
# MySQL 상태 확인
docker-compose ps

# MySQL 로그 확인
docker-compose logs mysql

# 재시작
docker-compose restart mysql
```

### 포트 이미 사용 중
```bash
# 기존 컨테이너 중지
docker-compose down

# 모든 컨테이너 제거
docker-compose down -v
```

### H2 테스트 실패
```bash
# 테스트 리소스 정리
./gradlew cleanTest

# 캐시 정리 후 재빌드
./gradlew clean test
```

## 🎓 추가 학습 자료

- [Spring Data JPA 문서](https://spring.io/projects/spring-data-jpa)
- [Hibernate 문서](https://hibernate.org/orm/documentation/)
- [MySQL 공식 문서](https://dev.mysql.com/doc/)
- [H2 데이터베이스](https://www.h2database.com/)

## 📞 문제 발생 시

1. 로그 확인: `spring.jpa.show-sql=true`로 설정하여 SQL 로그 확인
2. 테스트 실행: `./gradlew test`로 데이터베이스 연결 확인
3. 컨테이너 상태: `docker-compose logs`로 서비스 상태 확인
