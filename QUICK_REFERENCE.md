# 🚀 Flash Sale System - 빠른 참조 가이드

## ⚡ 30초 요약

```
Multi-Tier (3계층)
├─ 🛂 Controller: HTTP 요청 처리
├─ 💼 Service: 비즈니스 로직
└─ 🗄️ Repository: 데이터 접근

Multi-Thread (동시성)
├─ ExecutorService: 10개 스레드 풀
├─ ConcurrentHashMap: 스레드 안전 자료구조
├─ Redis Lock: 분산 시스템 동시성 제어
└─ Kafka Consumer: 병렬 메시지 처리

Distributed (분산 시스템)
├─ Redis: 공유 캐시 & 분산 잠금
├─ Kafka: 메시지 큐 & 내구성
└─ MySQL: 중앙 데이터베이스
```

## 📁 5분안에 폴더 구조 이해하기

| 폴더 | 계층 | 역할 | 핵심 파일 |
|-----|------|------|---------|
| **controller/** | 1 | HTTP 요청/응답 | OrderController |
| **service/** | 2 | 비즈니스 로직 | FlashSaleService, PaymentService |
| **domain/** | - | 도메인 모델 | Order, Product, Inventory |
| **entity/** | - | JPA 매핑 | OrderEntity, ProductEntity |
| **repository/** | 3 | 데이터 접근 | OrderJpaRepository |
| **config/** | - | 설정 | RedisConfig, KafkaConfig, ExecutorConfig |
| **exception/** | - | 예외 처리 | GlobalExceptionHandler |

## ▶️ 5분안에 앱 실행하기

```bash
# 1️⃣ 터미널 1: Docker 서비스 시작
docker-compose up -d

# 2️⃣ 터미널 2: 애플리케이션 시작
cd /workspaces/Java_Multithread
./gradlew bootRun

# 3️⃣ 터미널 3: API 테스트
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"SKU-1001","userId":"user-1","quantity":2}' | jq .

# 응답:
# {
#   "orderId": "550e8400-...",
#   "status": "ACCEPTED",
#   "remainingStock": 98
# }
```

## 🔄 요청-응답 흐름 (한 페이지)

```
사용자 요청
  ↓ (JSON)
POST /api/orders
  ↓
OrderController.placeOrder()
  ├─ 요청 검증
  └─ FlashSaleService 호출
    ↓
  FlashSaleService.placeOrder()
    ├─ RedisLockService (분산 잠금 획득)
    ├─ 재고 확인 및 감소
    ├─ OrderEventProducer (Kafka 발행)
    └─ 응답 생성
      ↓
  OrderEventConsumer (Worker에서 비동기 처리)
    ├─ Kafka 메시지 수신
    ├─ 주문 정보 역직렬화
    └─ 데이터베이스 저장
      ↓
사용자에게 응답 반환 (ACCEPTED)
  ↓ (JSON)
{orderId, status, remainingStock, ...}
```

## 🧵 Multi-Thread 5가지 구현 위치

### 1️⃣ ExecutorConfig (스레드 풀)
```java
// 10개 스레드로 동시 처리
@Bean
public ExecutorService orderProcessingExecutor() {
    return Executors.newFixedThreadPool(10);
}
```

### 2️⃣ PaymentService (비동기 결제)
```java
// 별도 스레드에서 결제 처리
orderProcessingExecutor.submit(() -> {
    PaymentResult result = processPayment(...);
    paymentCache.put(paymentId, result);
});
```

### 3️⃣ FlashSaleService (동시성 제어)
```java
// ConcurrentHashMap + Redis Lock
private ConcurrentHashMap<String, Integer> inventory;
return redisLockService.withLock(lockKey, action);
```

### 4️⃣ RedisLockService (분산 잠금)
```java
// Redis SET NX로 분산 환경에서 안전
redisTemplate.opsForValue().setIfAbsent(lockKey, uniqueValue, Duration.ofSeconds(5));
```

### 5️⃣ OrderEventConsumer (Kafka 병렬)
```java
// @KafkaListener로 여러 파티션 병렬 처리
@KafkaListener(topics = "order-events", groupId = "order-processing-group")
public void handleOrderCreated(String message) { ... }
```

## 🏗️ Multi-Tier 3단계 설명

### TIER 1: 프레젠테이션 (Controller)
```
역할: HTTP 요청 ↔ 응답
파일: OrderController.java

@RestController
├─ POST /api/orders → placeOrder()
└─ GET /api/inventory → getInventorySnapshot()
```

### TIER 2: 비즈니스 로직 (Service)
```
역할: 주문 처리, 재고 관리, 결제, 메시지 발행
파일: 
├─ FlashSaleService.java (주문 처리)
├─ PaymentService.java (결제)
├─ OrderEventProducer.java (Kafka 발행)
└─ OrderEventConsumer.java (Kafka 수신)
```

### TIER 3: 데이터 접근 (Repository)
```
역할: 데이터베이스 추상화
파일: OrderJpaRepository, ProductJpaRepository, ...

public interface OrderJpaRepository 
    extends JpaRepository<OrderEntity, String> {
    List<OrderEntity> findByUserId(String userId);
    List<OrderEntity> findByStatus(Order.OrderStatus status);
}
```

## 🔗 외부 시스템 연결

| 시스템 | 포트 | 용도 | 설정 |
|------|------|------|------|
| **MySQL** | 3306 | 데이터 저장 | application.properties |
| **Redis** | 6379 | 캐시 & 분산 잠금 | RedisConfig.java |
| **Kafka** | 9092 | 메시지 큐 | KafkaConfig.java |
| **Zookeeper** | 2181 | Kafka 조정 | docker-compose.yml |

## 📊 데이터 구조

### OrderEntity (주문)
```
order_id (PK)          | 550e8400-...
user_id (FK)           | user-123
product_id (FK)        | SKU-1001
quantity               | 2
total_price            | 0.00
status                 | QUEUED
created_at             | 2026-08-13 18:00:00
```

### InventoryEntity (재고)
```
product_id (PK)        | SKU-1001
available_quantity     | 98
reserved_quantity      | 2
sold_quantity          | 0
last_updated           | 2026-08-13 18:00:01
```

## 🧪 테스트 실행

```bash
# 모든 테스트 (25개)
./gradlew test

# 특정 테스트만
./gradlew test --tests FlashSaleServiceTest
./gradlew test --tests JpaRepositoryIntegrationTest

# 테스트 리포트
xdg-open build/reports/tests/test/index.html

# 결과 확인
cat build/test-results/test/TEST-*.xml
```

## 🐛 문제 해결

| 문제 | 해결책 |
|-----|--------|
| **포트 이미 사용 중** | `docker-compose down -v` 후 다시 시작 |
| **데이터베이스 연결 안 됨** | `docker-compose logs mysql` 확인 |
| **테스트 실패** | `./gradlew cleanTest` 후 재실행 |
| **Kafka 메시지 안 옴** | `docker exec flash-sale-kafka kafka-console-consumer.sh ...` 확인 |
| **Redis 잠금 타임아웃** | `spring.jpa.properties.hibernate.jdbc.batch_size` 확인 |

## 🎯 핵심 개념 체크리스트

- [ ] Multi-Tier: Controller → Service → Repository
- [ ] Multi-Thread: ExecutorService, ConcurrentHashMap, Redis Lock
- [ ] Distributed: Redis (캐시), Kafka (메시지), MySQL (DB)
- [ ] 동시성 제어: ConcurrentHashMap + Redis Lock
- [ ] 비동기 처리: PaymentService with ExecutorService
- [ ] 메시지 처리: OrderEventProducer/Consumer with Kafka
- [ ] JPA: Entity → Repository → Service

## 📚 상세 문서

- [종합 가이드](./COMPREHENSIVE_GUIDE.md) - 1600줄의 상세 설명
- [데이터베이스 가이드](./DATABASE_SETUP_GUIDE.md) - DB 연결 상세 설명
- [진행 상황](./PROGRESS_REPORT.md) - 구현 현황
- [README](./README) - 프로젝트 개요

## 🚀 다음 단계

1. 프로젝트 실행해보기
2. API 요청 테스트하기
3. 데이터베이스에서 데이터 확인하기
4. 로그 확인해서 Multi-Thread 동작 이해하기
5. 코드 수정해서 커스터마이징하기

---

**Happy Coding!** 🎉
