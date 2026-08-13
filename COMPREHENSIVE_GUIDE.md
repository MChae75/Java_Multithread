# 🏗️ Distributed Flash Sale System - 완벽한 프로젝트 가이드

## 📑 목차
1. [프로젝트 전체 구조](#프로젝트-전체-구조)
2. [Multi-Tier Architecture 상세 설명](#multi-tier-architecture-상세-설명)
3. [Multi-Thread 구현 위치](#multi-thread-구현-위치)
4. [Distributed System 구조](#distributed-system-구조)
5. [폴더별 상세 설명](#폴더별-상세-설명)
6. [실행 방법 (완벽 가이드)](#실행-방법-완벽-가이드)
7. [데이터 흐름](#데이터-흐름)

---

## 🏗️ 프로젝트 전체 구조

```
Java_Multithread/
├── 📄 설정 및 문서
│   ├── build.gradle                 # Gradle 빌드 설정 및 의존성
│   ├── settings.gradle              # 프로젝트 설정
│   ├── docker-compose.yml           # Docker 서비스 정의
│   ├── init.sql                     # MySQL 초기화 스크립트
│   ├── README                       # 프로젝트 개요
│   ├── PROGRESS_REPORT.md           # 진행 상황 리포트
│   └── DATABASE_SETUP_GUIDE.md      # 데이터베이스 설정 가이드
│
├── 🔧 Gradle
│   ├── gradlew                      # Gradle Wrapper (Linux/Mac)
│   ├── gradlew.bat                  # Gradle Wrapper (Windows)
│   └── gradle/wrapper/              # Gradle 라이브러리
│
└── 📁 소스 코드
    ├── src/main/java/com/example/flashsale/
    │   ├── 🎯 FlashSaleApplication.java     # Spring Boot 메인 애플리케이션
    │   │
    │   ├── 🛂 controller/                   # [TIER 1: Presentation Layer]
    │   │   └── OrderController.java         # REST API 엔드포인트
    │   │
    │   ├── 💼 service/                      # [TIER 2: Business Logic Layer]
    │   │   ├── FlashSaleService.java        # 주문 처리 서비스 (메모리)
    │   │   ├── FlashSaleServiceDb.java      # 주문 처리 서비스 (DB)
    │   │   ├── PaymentService.java          # 결제 처리 서비스
    │   │   ├── OrderEventProducer.java      # Kafka 프로듀서
    │   │   ├── OrderEventConsumer.java      # Kafka 컨슈머
    │   │   ├── RedisLockService.java        # Redis 분산 잠금
    │   │   ├── PaymentRequest.java          # 결제 요청 DTO
    │   │   └── PaymentResult.java           # 결제 결과 DTO
    │   │
    │   ├── 📊 domain/                       # [Shared: Domain Models]
    │   │   ├── Order.java                   # 주문 도메인 모델
    │   │   ├── Product.java                 # 상품 도메인 모델
    │   │   ├── Inventory.java               # 재고 도메인 모델
    │   │   └── OrderRequest.java            # 주문 요청 DTO
    │   │
    │   ├── 💾 entity/                       # [JPA Entities]
    │   │   ├── OrderEntity.java             # Order 엔티티 (JPA)
    │   │   ├── ProductEntity.java           # Product 엔티티 (JPA)
    │   │   ├── InventoryEntity.java         # Inventory 엔티티 (JPA)
    │   │   └── PaymentEntity.java           # Payment 엔티티 (JPA)
    │   │
    │   ├── 🗄️ repository/                   # [TIER 3: Data Access Layer]
    │   │   ├── OrderRepository.java         # Order 저장소 인터페이스
    │   │   ├── ProductRepository.java       # Product 저장소 인터페이스
    │   │   ├── InventoryRepository.java     # Inventory 저장소 인터페이스
    │   │   │
    │   │   ├── impl/                        # 인메모리 구현 (테스트용)
    │   │   │   ├── InMemoryOrderRepository.java
    │   │   │   ├── InMemoryProductRepository.java
    │   │   │   └── InMemoryInventoryRepository.java
    │   │   │
    │   │   └── jpa/                         # JPA 구현 (프로덕션용)
    │   │       ├── OrderJpaRepository.java
    │   │       ├── ProductJpaRepository.java
    │   │       ├── InventoryJpaRepository.java
    │   │       └── PaymentJpaRepository.java
    │   │
    │   ├── ⚙️ config/                       # [Configuration]
    │   │   ├── RedisConfig.java             # Redis 연결 설정
    │   │   ├── KafkaConfig.java             # Kafka 컨슈머 설정
    │   │   └── ExecutorConfig.java          # Thread Pool 설정
    │   │
    │   └── ⚠️ exception/                    # [Exception Handling]
    │       ├── GlobalExceptionHandler.java  # 전역 예외 처리
    │       ├── InsufficientInventoryException.java
    │       └── DistributedLockException.java
    │
    ├── src/main/resources/
    │   └── application.properties           # Spring Boot 설정 (프로덕션)
    │
    └── src/test/
        ├── java/com/example/flashsale/      # 테스트 클래스
        │   ├── FlashSaleServiceTest.java
        │   ├── PaymentServiceTest.java
        │   ├── InventoryTest.java
        │   ├── RepositoryTest.java
        │   ├── JpaRepositoryIntegrationTest.java
        │   └── SimpleDatabaseTest.java
        │
        └── resources/
            ├── application-test.properties  # Spring Boot 설정 (테스트)
            └── schema.sql                   # H2 테스트 스키마
```

---

## 🏗️ Multi-Tier Architecture 상세 설명

### 개념: 3계층 분리로 관심사 분리 (Separation of Concerns)

```
┌─────────────────────────────────────────────────────────────┐
│                  🛂 TIER 1: PRESENTATION LAYER              │
│                      (프레젠테이션 계층)                      │
│  - HTTP 요청/응답 처리                                       │
│  - 요청 검증 및 응답 포맷팅                                  │
│  - OrderController                                          │
│  - GlobalExceptionHandler                                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    HTTP 통신 (REST API)
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  💼 TIER 2: BUSINESS LOGIC LAYER            │
│                    (비즈니스 로직 계층)                       │
│  - 주문 처리 로직                                            │
│  - 재고 확인 및 감소                                         │
│  - 결제 처리                                                │
│  - 분산 잠금 (Redis)                                         │
│  - 메시지 발행 (Kafka)                                       │
│  - FlashSaleService/FlashSaleServiceDb                     │
│  - PaymentService                                          │
│  - RedisLockService                                        │
│  - OrderEventProducer/OrderEventConsumer                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    데이터 쿼리/저장
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  🗄️ TIER 3: DATA ACCESS LAYER              │
│                    (데이터 접근 계층)                         │
│  - 데이터 저장소 추상화                                      │
│  - Repository 패턴 구현                                     │
│  - ORM 처리 (Hibernate/JPA)                                 │
│  - OrderRepository/OrderJpaRepository                       │
│  - ProductRepository/ProductJpaRepository                   │
│  - InventoryRepository/InventoryJpaRepository              │
│  - PaymentJpaRepository                                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    SQL 쿼리 실행
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  🗃️ DATABASE LAYER (외부 시스템)             │
│  - MySQL (프로덕션)                                          │
│  - H2 (테스트)                                              │
│  - Redis (분산 잠금)                                         │
│  - Kafka (메시지 큐)                                        │
└─────────────────────────────────────────────────────────────┘
```

### 각 계층의 책임

| 계층 | 책임 | 클래스 | 역할 |
|-----|------|-------|------|
| **1. 프레젠테이션** | HTTP 처리 | OrderController | POST /api/orders, GET /api/inventory |
| **1. 프레젠테이션** | 예외 처리 | GlobalExceptionHandler | HTTP 에러 응답 |
| **2. 비즈니스 로직** | 주문 처리 | FlashSaleService<br/>FlashSaleServiceDb | 재고 확인, 주문 생성, Kafka 발행 |
| **2. 비즈니스 로직** | 결제 처리 | PaymentService | 비동기 결제 처리 |
| **2. 비즈니스 로직** | 분산 잠금 | RedisLockService | Race condition 방지 |
| **2. 비즈니스 로직** | 메시지 처리 | OrderEventProducer<br/>OrderEventConsumer | Kafka 통신 |
| **3. 데이터 접근** | 추상화 | Repository 인터페이스 | 데이터 접근 규칙 |
| **3. 데이터 접근** | JPA 구현 | *JpaRepository | DB 작업 |
| **3. 데이터 접근** | 인메모리 | InMemory*Repository | 테스트용 |

### 계층 간 통신 흐름

```
1️⃣ 클라이언트 요청
   ↓
2️⃣ OrderController.placeOrder()
   - 요청 검증 (OrderRequest)
   - FlashSaleService 호출
   ↓
3️⃣ FlashSaleService.placeOrder()
   - 비즈니스 로직 처리
   - RedisLockService로 잠금 획득
   - OrderRepository로 데이터 접근
   - OrderEventProducer로 Kafka 발행
   ↓
4️⃣ OrderRepository (JPA)
   - Hibernate ORM을 통해 SQL 생성
   - MySQL에 쿼리 실행
   ↓
5️⃣ 응답 생성 및 반환
   - OrderController → JSON → 클라이언트
```

### 계층 분리의 이점

```
✅ 관심사 분리 (Separation of Concerns)
   - 각 계층이 자신의 책임에만 집중
   
✅ 테스트 용이성
   - Mock Repository로 쉽게 단위 테스트
   - 계층별 독립적인 테스트 가능
   
✅ 재사용성
   - Service는 Controller/Test 양쪽에서 재사용
   - Repository는 다양한 구현 교체 가능
   
✅ 유지보수성
   - 변경의 영향 범위 최소화
   - 예: 데이터베이스 변경 → Repository만 수정
   
✅ 확장성
   - 새로운 기능 추가 용이
   - 계층 구조 유지하며 확장
```

---

## 🔄 Multi-Thread 구현 위치

### 1. ExecutorConfig - Thread Pool 설정

```java
// 📁 config/ExecutorConfig.java
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean
    public ExecutorService orderProcessingExecutor() {
        // 스레드 풀: 10개 스레드로 동시 주문 처리
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutor() {
        // 스케줄 실행기: 5개 스레드로 주기적 작업
        return Executors.newScheduledThreadPool(5);
    }
}

// 사용 위치:
// - PaymentService: 결제를 비동기로 처리
// - OrderEventConsumer: Kafka 메시지 병렬 처리
```

### 2. PaymentService - 비동기 결제 처리

```java
// 📁 service/PaymentService.java
@Service
public class PaymentService {
    
    private final ExecutorService orderProcessingExecutor;
    
    // 🔵 Multi-Thread: 결제를 별도 스레드에서 비동기 처리
    public PaymentResult processPayment(PaymentRequest request) {
        orderProcessingExecutor.submit(() -> {
            // 이 코드가 별도의 스레드에서 실행됨
            PaymentResult result;
            if ("CREDIT_CARD".equalsIgnoreCase(request.getPaymentMethod())) {
                result = processCreditCardPayment(paymentId, request);
            } else if ("DEBIT_CARD".equalsIgnoreCase(request.getPaymentMethod())) {
                result = processDebitCardPayment(paymentId, request);
            }
            // 결과를 캐시에 저장
            paymentCache.put(paymentId, result);
        });
        
        // 즉시 PENDING 상태로 반환 (비동기)
        return new PaymentResult(paymentId, request.getOrderId(), 
                PaymentResult.PaymentStatus.PENDING, "Processing");
    }
}
```

**Multi-Thread 이점:**
- 결제 처리 시간(100-500ms)이 사용자 응답에 영향 없음
- 여러 사용자의 결제를 동시에 처리
- 총 처리량(throughput) 증가

### 3. FlashSaleService - 동시성 제어

```java
// 📁 service/FlashSaleService.java
public class FlashSaleService {
    
    // 🔵 스레드 안전: ConcurrentHashMap 사용
    private final ConcurrentHashMap<String, Integer> inventory;
    
    public Map<String, Object> placeOrder(OrderRequest request) {
        String productId = request.productId();
        
        // 🔴 Critical Section: Redis 분산 잠금으로 보호
        return withInventoryLock(productId, () -> {
            int currentStock = inventory.getOrDefault(productId, 0);
            
            if (currentStock < request.quantity()) {
                throw new IllegalStateException("Not enough stock");
            }
            
            // 원자적 연산: 재고 감소
            int remainingStock = currentStock - request.quantity();
            inventory.put(productId, remainingStock);
            
            return Map.of("status", "ACCEPTED", "remainingStock", remainingStock);
        });
    }
    
    // 🔵 Multi-Thread: Redis 또는 synchronized 잠금
    private <T> T withInventoryLock(String productId, Supplier<T> action) {
        if (redisLockService != null) {
            // Redis 분산 잠금 (여러 서버에서 안전)
            return redisLockService.withLock("inventory:" + productId, action);
        }
        // 단일 서버: synchronized 사용
        synchronized (this) {
            return action.get();
        }
    }
}
```

**동시성 제어 메커니즘:**
```
여러 스레드 또는 프로세스가 동시에 같은 상품에 접근할 때:

Without Lock:
Thread 1: stock=100, read 100
Thread 2: stock=100, read 100
Thread 1: 2개 구매 → stock=98로 업데이트
Thread 2: 2개 구매 → stock=98로 업데이트 (❌ 잘못됨! 96이어야 함)

With Redis Lock:
Thread 1: LOCK 획득 → stock=100, read 100 → 2개 구매 → stock=98 → LOCK 해제
Thread 2: 대기 중...
Thread 2: LOCK 획득 → stock=98, read 98 → 2개 구매 → stock=96 → LOCK 해제
(✅ 올바름)
```

### 4. OrderEventConsumer - Kafka 병렬 처리

```java
// 📁 service/OrderEventConsumer.java
@Service
public class OrderEventConsumer {
    
    // 🔵 Multi-Thread: @KafkaListener로 자동 스레드 풀 생성
    // Kafka는 여러 파티션에서 병렬로 메시지를 소비
    @KafkaListener(topics = "order-events", groupId = "order-processing-group")
    public void handleOrderCreated(String message) {
        // 각 메시지가 별도의 스레드에서 처리됨
        Map<String, Object> orderEvent = objectMapper.readValue(message, Map.class);
        
        // 이 로직이 병렬로 실행됨
        redisLockService.withLock("process:" + orderId, () -> {
            // 주문 처리 (DB 업데이트 등)
            return null;
        });
    }
}
```

**Kafka 병렬 처리:**
```
Kafka Topic: order-events (4 partitions)

Producer:
Thread-1: 메시지 1 → Partition 0
Thread-2: 메시지 2 → Partition 1
Thread-3: 메시지 3 → Partition 2
Thread-4: 메시지 4 → Partition 3

Consumer (Consumer Group):
Consumer-Thread-1: Partition 0 메시지 처리 (병렬)
Consumer-Thread-2: Partition 1 메시지 처리 (병렬)
Consumer-Thread-3: Partition 2 메시지 처리 (병렬)
Consumer-Thread-4: Partition 3 메시지 처리 (병렬)
```

### 5. RedisLockService - 분산 잠금

```java
// 📁 service/RedisLockService.java
@Service
public class RedisLockService {
    
    private final StringRedisTemplate redisTemplate;
    
    public <T> T withLock(String lockKey, Supplier<T> action) {
        // 🔵 Unique value로 잠금 소유권 보장
        String uniqueValue = UUID.randomUUID().toString();
        
        // Redis SET NX (set if not exists) + 만료시간 설정
        boolean acquired = Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(
                lockKey, 
                uniqueValue, 
                Duration.ofSeconds(5)  // 5초 후 자동 해제
            )
        );
        
        if (!acquired) {
            throw new IllegalStateException("Could not acquire lock");
        }
        
        try {
            return action.get();  // Critical section 실행
        } finally {
            // 자신이 획득한 잠금만 해제
            if (uniqueValue.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}

// 🔵 Multi-Server 동시성 제어:
// Server A: inventory:SKU-1001 LOCK
// Server B: inventory:SKU-1001 대기 (Redis에서 차단)
// Server C: inventory:SKU-1001 대기 (Redis에서 차단)
```

### 6. Test: 동시성 테스트

```java
// 📁 test/FlashSaleServiceTest.java
@Test
void placeOrder_shouldPreventRaceCondition_underConcurrentRequests() 
    throws InterruptedException {
    
    String productId = "SKU-2001";
    FlashSaleService service = new FlashSaleService(
        Map.of(productId, 100)  // 100개 재고
    );
    
    // 🔵 Multi-Thread Test: 50개의 스레드가 동시에 요청
    int threadCount = 50;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);  // 모든 스레드를 동시에 시작
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            startLatch.await();  // 모든 스레드가 준비될 때까지 대기
            // 동시에 2개씩 구매 시도
            service.placeOrder(new OrderRequest(productId, 2, "user-" + i));
        });
    }
    
    startLatch.countDown();  // 모든 스레드 시작!
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
    
    // ✅ 검증: 정확히 50명이 구매 성공, 재고는 0
    int remainingStock = service.getInventorySnapshot()
        .getOrDefault(productId, 0);
    assertEquals(0, remainingStock);  // 동시성 제어 성공!
}
```

### Multi-Thread 요약

| 구성 요소 | 역할 | 스레드 수 | 목적 |
|---------|------|---------|------|
| **ExecutorConfig** | Thread Pool 설정 | 10 (기본) | 비동기 작업 관리 |
| **PaymentService** | 결제 비동기 처리 | ExecutorService | 응답 지연 방지 |
| **FlashSaleService** | 동시성 제어 | 여러 스레드 | Race condition 방지 |
| **RedisLockService** | 분산 잠금 | N/A | 다중 서버 동시성 |
| **OrderEventConsumer** | Kafka 병렬 처리 | Kafka Concurrency | 메시지 병렬 처리 |
| **ConcurrentHashMap** | 스레드 안전 자료구조 | 여러 스레드 | 동시 접근 안전 |

---

## 🌐 Distributed System 구조

### 분산 시스템의 핵심 개념

```
┌─────────────────────────────────────────────────────────────┐
│                   Distributed Flash Sale                    │
│  여러 대의 서버가 협력하여 대규모 트래픽 처리              │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│   Server A       │    │   Server B       │    │   Server C       │
│   (API Server 1) │    │   (API Server 2) │    │   (Worker 1)     │
├──────────────────┤    ├──────────────────┤    ├──────────────────┤
│ OrderController  │    │ OrderController  │    │ OrderEventConsumer
│ FlashSaleService │    │ FlashSaleService │    │ ProcessOrder     │
└────────┬─────────┘    └────────┬─────────┘    └────────┬─────────┘
         │                      │                        │
         └──────────────────────┼────────────────────────┘
                                │
                  ┌─────────────┴─────────────┐
                  │                           │
         ┌────────▼────────┐        ┌────────▼────────┐
         │  Redis (Cache   │        │  Kafka (Message │
         │  + Lock)        │        │  Queue)         │
         │  localhost:6379 │        │  localhost:9092 │
         └────────┬────────┘        └────────┬────────┘
                  │                          │
                  └──────────────┬───────────┘
                                 │
                         ┌───────▼────────┐
                         │  MySQL (DB)    │
                         │ localhost:3306 │
                         └────────────────┘
```

### 분산 시스템의 세 가지 특징

#### 1️⃣ 분산 캐시 (Redis)

```java
// 여러 서버가 공유하는 캐시
// Server A와 Server B가 같은 Redis 인스턴스 사용

// 장점:
// - 모든 서버가 같은 데이터 보유
// - 서버 간 데이터 일관성 유지
// - 빠른 조회 성능

// RedisLockService 사용 예:
redisLockService.withLock("inventory:SKU-1001", () -> {
    // 이 코드는 모든 서버에서 순차적으로 실행됨
    // 동시에 한 대의 서버만 실행 가능
    deductInventory(productId, quantity);
    return true;
});
```

#### 2️⃣ 분산 메시지 큐 (Kafka)

```
API Server에서 요청 받음 (빠른 응답)
    ↓
Kafka에 메시지 발행 (순서 보장, 내구성)
    ↓
Worker Server에서 메시지 소비 (병렬 처리)
    ↓
데이터베이스에 저장

// 장점:
// - API 서버는 빠른 응답 제공
// - Worker 서버들이 병렬로 처리
// - 메시지 손실 없음 (내구성)
// - 서버 간 느슨한 결합 (Loose Coupling)
```

#### 3️⃣ 분산 데이터베이스 (MySQL + Replication)

```
Master DB (Write)
    ↓ 복제
┌───────────────┬───────────────┐
│   Slave 1     │   Slave 2     │
│   (Read)      │   (Read)      │
└───────────────┴───────────────┘

Read/Write Replication:
- Write: 모든 요청이 Master로 (일관성)
- Read: Slave에서 부하 분산 (성능)
```

### 분산 시스템 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│              Cloud Load Balancer (Load Balancing)            │
│  요청을 여러 API 서버로 분산                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
       ┌───────────────┼───────────────┐
       │               │               │
┌──────▼────────┐ ┌──────▼────────┐ ┌──────▼────────┐
│   API Server 1│ │   API Server 2│ │   API Server 3│
│ Port 8080     │ │ Port 8080     │ │ Port 8080     │
└──────┬────────┘ └──────┬────────┘ └──────┬────────┘
       │                 │                 │
       │  (1) 요청 처리   │                 │
       │  (2) 재고 확인   │                 │
       │  (3) Kafka 발행  │                 │
       │                 │                 │
       └─────────────────┼─────────────────┘
                         │
       ┌─────────────────┴──────────────────┐
       │                                    │
  ┌────▼────────────────────────┐    ┌────▼────────────────────────┐
  │   Redis (분산 캐시/잠금)     │    │   Kafka (메시지 큐)         │
  │   - 재고 빠른 조회           │    │   - 주문 메시지 저장        │
  │   - 분산 잠금 (SKU별)        │    │   - Topic: order-events    │
  │   - 결제 상태 캐시           │    │   - 내구성 있는 처리        │
  │   localhost:6379            │    │   localhost:9092           │
  └────┬────────────────────────┘    └────┬────────────────────────┘
       │                                   │
       │        ┌────────────────────┐    │
       │        │   Worker Servers   │────┘
       │        │   (Order Processing)
       │        │   - Consumer 1     │
       │        │   - Consumer 2     │
       │        │   - Consumer 3     │
       │        └────────┬───────────┘
       │                 │
       │        ┌────────▼────────────────────┐
       │        │   MySQL Database            │
       │        │   - Master: Write Only      │
       │        │   - Slave 1: Read Only      │
       │        │   - Slave 2: Read Only      │
       │        │   localhost:3306           │
       │        └────────────────────────────┘
       │                 ▲
       │                 │
       └─────────────────┘
         (공유 자원에 접근)
```

---

## 📁 폴더별 상세 설명

### 1️⃣ controller/ (프레젠테이션 계층)

```
controller/
└── OrderController.java

📝 역할: HTTP 요청/응답 처리의 진입점

🔍 상세:
├─ @RestController
│  └─ REST API 엔드포인트로 표시
│
├─ @RequestMapping("/api")
│  └─ 모든 엔드포인트는 /api로 시작
│
├─ POST /api/orders
│  ├─ 요청: OrderRequest (productId, quantity, userId)
│  ├─ 처리: FlashSaleService.placeOrder() 호출
│  └─ 응답: {orderId, status, remainingStock, ...}
│
└─ GET /api/inventory
   ├─ 처리: FlashSaleService.getInventorySnapshot() 호출
   └─ 응답: {SKU-1001: 98, SKU-1002: 48, ...}

💡 핵심 개념:
- 요청 검증 (null 체크, 타입 변환)
- 서비스 호출 (비즈니스 로직 위임)
- 응답 포맷팅 (JSON 직렬화)
- 예외 처리 (GlobalExceptionHandler로 위임)
```

**OrderController 코드 흐름:**

```java
@RestController
@RequestMapping("/api")
public class OrderController {
    
    private final FlashSaleService flashSaleService;
    
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> placeOrder(
        @RequestBody OrderRequest request
    ) {
        // Step 1: 요청 데이터 검증 (Spring이 자동 처리)
        // Step 2: 비즈니스 로직 호출
        Map<String, Object> response = flashSaleService.placeOrder(request);
        
        // Step 3: 응답 반환
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Integer>> inventory() {
        return ResponseEntity.ok(flashSaleService.getInventorySnapshot());
    }
}
```

### 2️⃣ service/ (비즈니스 로직 계층)

```
service/
├── FlashSaleService.java           # 주문 처리 (메모리)
├── FlashSaleServiceDb.java         # 주문 처리 (DB)
├── PaymentService.java             # 결제 처리 (비동기)
├── OrderEventProducer.java         # Kafka 프로듀서
├── OrderEventConsumer.java         # Kafka 컨슈머
├── RedisLockService.java           # 분산 잠금
├── PaymentRequest.java             # 결제 요청 DTO
└── PaymentResult.java              # 결제 결과 DTO

📝 역할: 핵심 비즈니스 로직 구현

🔍 FlashSaleService (메모리 기반):
├─ placeOrder()
│  ├─ 요청 검증
│  ├─ RedisLockService로 재고 잠금
│  ├─ 재고 확인 및 감소
│  ├─ Kafka 메시지 발행
│  └─ 응답 반환
│
└─ getInventorySnapshot()
   └─ 현재 재고 스냅샷 반환

🔍 FlashSaleServiceDb (DB 기반):
├─ placeOrder()
│  ├─ 요청 검증
│  ├─ OrderJpaRepository로 주문 저장
│  ├─ InventoryJpaRepository로 재고 업데이트
│  └─ @Transactional로 원자성 보장
│
└─ getUserOrders()
   └─ 사용자의 모든 주문 조회

🔍 PaymentService (비동기):
├─ processPayment()
│  ├─ ExecutorService로 별도 스레드에서 실행
│  ├─ 결제 방식별 처리 (CREDIT_CARD, DEBIT_CARD, WALLET)
│  └─ 결과를 캐시에 저장
│
└─ checkPaymentStatus()
   └─ 결제 상태 조회

🔍 OrderEventProducer (Kafka):
├─ publishOrderCreated()
│  ├─ ObjectMapper로 JSON 직렬화
│  └─ KafkaTemplate로 메시지 발행

🔍 OrderEventConsumer (Kafka):
├─ handleOrderCreated()
│  ├─ @KafkaListener로 메시지 수신
│  ├─ 역직렬화
│  └─ 주문 처리

🔍 RedisLockService (분산 잠금):
├─ withLock()
│  ├─ Redis SET NX로 잠금 획득
│  ├─ Action 실행 (Critical Section)
│  └─ 잠금 해제
```

**서비스 간 호출 관계:**

```
OrderController
    ↓
FlashSaleService (또는 FlashSaleServiceDb)
    ├─ RedisLockService (동시성 제어)
    ├─ OrderEventProducer (메시지 발행)
    └─ Repository (데이터 접근)
    
PaymentService
    ├─ ExecutorService (비동기)
    └─ 결제 캐시 (PaymentResult 저장)

OrderEventConsumer
    ├─ OrderEventProducer (메시지 수신)
    └─ RedisLockService (처리 중 잠금)
```

### 3️⃣ domain/ (도메인 모델)

```
domain/
├── Order.java            # 주문 도메인 모델
├── Product.java          # 상품 도메인 모델
├── Inventory.java        # 재고 도메인 모델
└── OrderRequest.java     # 요청 DTO

📝 역할: 비즈니스 개념을 코드로 표현

🔍 Order:
├─ orderId: String
├─ userId: String
├─ productId: String
├─ quantity: int
├─ totalPrice: BigDecimal
├─ status: Enum (QUEUED, PROCESSING, CONFIRMED, SHIPPED, CANCELLED)
└─ timestamps: createdAt, updatedAt

🔍 Product:
├─ productId: String
├─ name: String
├─ price: BigDecimal
├─ description: String
└─ totalStock: int

🔍 Inventory (스레드 안전):
├─ productId: String
├─ availableQuantity: int (구매 가능)
├─ reservedQuantity: int (예약됨)
├─ soldQuantity: int (판매됨)
├─ reserve()         # 동시성 제어 (synchronized)
├─ confirmSale()     # 확정 및 판매 수량 증가
├─ releaseReservation() # 예약 취소
└─ getTotalStock()   # 총 재고 (= available + reserved + sold)

🔍 OrderRequest (DTO):
├─ productId: String
├─ quantity: int
└─ userId: String

💡 핵심:
- 도메인 모델은 비즈니스 규칙을 캡슐화
- 순수 자바 객체 (POJO)
- Entity와 다름 (Database와 무관)
```

### 4️⃣ entity/ (JPA 엔티티)

```
entity/
├── OrderEntity.java       # @Entity
├── ProductEntity.java     # @Entity
├── InventoryEntity.java   # @Entity
└── PaymentEntity.java     # @Entity

📝 역할: 데이터베이스 테이블을 자바 객체로 매핑

🔍 주요 특징:
├─ @Entity: 데이터베이스 테이블로 매핑
├─ @Table(name="orders"): 테이블명 지정
├─ @Column(name="..."), @Id: 컬럼 정보
├─ @Enumerated(EnumType.STRING): Enum 타입 매핑
├─ @ManyToOne, @OneToMany: 관계 설정
└─ timestamps: JPA가 자동으로 관리

🔍 OrderEntity 구조:
@Entity
@Table(name = "orders")
public class OrderEntity {
    
    @Id
    @Column(name = "order_id")
    private String orderId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Order.OrderStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

💡 Entity vs Domain Model:
Domain (비즈니스):
    - Order.java (순수 자바)
    - 비즈니스 규칙 집중
    
Entity (데이터베이스):
    - OrderEntity.java (@Entity)
    - 테이블 구조 반영
    - JPA 어노테이션 포함
```

### 5️⃣ repository/ (데이터 접근 계층)

```
repository/
├── OrderRepository.java        # 인터페이스 (추상화)
├── ProductRepository.java      # 인터페이스
├── InventoryRepository.java    # 인터페이스
│
├── impl/                       # 인메모리 구현 (테스트용)
│   ├── InMemoryOrderRepository.java
│   ├── InMemoryProductRepository.java
│   └── InMemoryInventoryRepository.java
│
└── jpa/                        # JPA 구현 (프로덕션용)
    ├── OrderJpaRepository.java
    ├── ProductJpaRepository.java
    ├── InventoryJpaRepository.java
    └── PaymentJpaRepository.java

📝 역할: 데이터 접근 로직의 추상화 (Repository Pattern)

🔍 Repository Pattern의 핵심:
┌─────────────────────────────────┐
│    Service Layer                │
│    (비즈니스 로직)               │
└──────────────┬──────────────────┘
               │
               ↓ (Repository 인터페이스 사용)
┌─────────────────────────────────┐
│    Repository Interface         │
│    - save()                     │
│    - findById()                 │
│    - findAll()                  │
│    - delete()                   │
└──────────────┬──────────────────┘
               │
       ┌───────┴────────┐
       │                │
    ┌──▼───┐         ┌──▼──────┐
    │ impl │         │ jpa     │
    │      │         │         │
    │Memory│  또는   │Spring   │
    │DB    │         │Data     │
    └──────┘         └─────────┘

💡 이점:
✅ Database 변경에 영향 없음
   (MySQL → PostgreSQL 변경 시 impl만 수정)
   
✅ 테스트 용이
   (InMemory로 실제 DB 없이 테스트)
   
✅ 결합도 낮음
   (Service는 인터페이스에만 의존)

🔍 JPA Repository 특징:
// 인터페이스만 정의하면 Spring Data JPA가 구현 생성
public interface OrderJpaRepository 
    extends JpaRepository<OrderEntity, String> {
    
    // 메서드명만 정의하면 SQL 자동 생성
    List<OrderEntity> findByUserId(String userId);
    
    List<OrderEntity> findByStatus(Order.OrderStatus status);
    
    List<OrderEntity> findByProductId(String productId);
}

// Spring Data JPA가 내부적으로 SQL 생성:
// SELECT * FROM orders WHERE user_id = ?
// SELECT * FROM orders WHERE status = ?
```

### 6️⃣ config/ (설정)

```
config/
├── RedisConfig.java      # Redis 연결 설정
├── KafkaConfig.java      # Kafka 컨슈머 설정
└── ExecutorConfig.java   # Thread Pool 설정

📝 역할: 외부 시스템과의 연결 설정

🔍 RedisConfig:
@Configuration
public class RedisConfig {
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(
        RedisConnectionFactory connectionFactory
    ) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // Serializer 설정 (String ↔ Redis)
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        
        return template;
    }
}

🔍 KafkaConfig:
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
        ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        
        // 동시성 설정 (3개의 컨슈머 스레드)
        factory.setConcurrency(3);
        
        // 배치 ACK 모드 (성능 최적화)
        factory.getContainerProperties()
            .setAckMode(ContainerProperties.AckMode.BATCH);
        
        return factory;
    }
}

🔍 ExecutorConfig:
@Configuration
@EnableAsync
public class ExecutorConfig {
    
    @Bean
    public ExecutorService orderProcessingExecutor() {
        // 10개의 스레드로 주문 처리 병렬화
        return Executors.newFixedThreadPool(10);
    }
    
    @Bean
    public ScheduledExecutorService scheduledExecutor() {
        // 5개의 스레드로 주기적 작업 처리
        return Executors.newScheduledThreadPool(5);
    }
}
```

### 7️⃣ exception/ (예외 처리)

```
exception/
├── GlobalExceptionHandler.java
├── InsufficientInventoryException.java
└── DistributedLockException.java

📝 역할: 전역 예외 처리 및 사용자 정의 예외

🔍 GlobalExceptionHandler:
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 모든 @RestController에서 발생한 예외를 처리
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
        IllegalArgumentException ex
    ) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid input",
            ex.getMessage()
        );
    }
    
    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<Map<String, Object>> 
        handleInsufficientInventoryException(
            InsufficientInventoryException ex
        ) {
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "Inventory error",
            ex.getMessage()
        );
    }
    
    // 응답 포맷 통일
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
        HttpStatus status,
        String error,
        String message
    ) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}

💡 이점:
✅ 일관된 에러 응답 형식
✅ 예외 처리 로직 중앙화
✅ 서비스에서 예외 처리 코드 제거 가능
```

---

## 🚀 실행 방법 (완벽 가이드)

### Phase 1: 환경 준비

#### Step 1-1: 프로젝트 클론 또는 네비게이션

```bash
cd /workspaces/Java_Multithread
```

#### Step 1-2: 의존성 확인

```bash
# Gradle 버전 확인
./gradlew --version

# 의존성 다운로드
./gradlew build -x test
```

### Phase 2: Docker 서비스 시작

#### Step 2-1: 모든 서비스 시작

```bash
# Redis, Kafka, Zookeeper, MySQL 시작
docker-compose up -d
```

#### Step 2-2: 서비스 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker-compose ps

# 출력 예:
# NAME                    STATUS
# flash-sale-redis        Up 2 minutes
# flash-sale-zookeeper    Up 2 minutes
# flash-sale-kafka        Up 2 minutes
# flash-sale-mysql        Up 2 minutes
```

#### Step 2-3: 각 서비스 상태 확인

```bash
# Redis 상태
docker-compose logs redis | tail -5

# MySQL 상태
docker-compose logs mysql | tail -10

# Kafka 상태
docker-compose logs kafka | tail -10
```

#### Step 2-4: 각 서비스 포트 테스트

```bash
# Redis 연결 테스트
redis-cli -h localhost -p 6379 ping
# 출력: PONG

# MySQL 연결 테스트
mysql -h localhost -u root -ppassword -e "SELECT VERSION();"

# Kafka 토픽 확인
docker exec flash-sale-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Phase 3: 애플리케이션 빌드 및 테스트

#### Step 3-1: 전체 빌드

```bash
./gradlew clean build
```

#### Step 3-2: 컴파일만 (테스트 제외)

```bash
./gradlew compileJava compileTestJava
```

#### Step 3-3: 단위 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests FlashSaleServiceTest
./gradlew test --tests JpaRepositoryIntegrationTest
./gradlew test --tests SimpleDatabaseTest
```

#### Step 3-4: 테스트 결과 확인

```bash
# 테스트 리포트 열기 (Linux)
xdg-open build/reports/tests/test/index.html

# 테스트 리포트 열기 (Mac)
open build/reports/tests/test/index.html

# 테스트 결과 XML 확인 (콘솔)
cat build/test-results/test/TEST-com.example.flashsale.*.xml
```

### Phase 4: 애플리케이션 실행

#### Step 4-1: Spring Boot 실행

```bash
./gradlew bootRun
```

또는

```bash
# JAR 빌드 후 직접 실행
./gradlew bootJar
java -jar build/libs/flash-sale-system-0.0.1-SNAPSHOT.jar
```

#### Step 4-2: 실행 확인

```bash
# 애플리케이션 로그 확인 (다른 터미널에서)
# 다음 메시지가 나타나면 정상:
# "Tomcat started on port(s): 8080"
# "Started FlashSaleApplication in X.XXX seconds"
```

### Phase 5: API 테스트

#### Step 5-1: 재고 조회

```bash
curl http://localhost:8080/api/inventory | jq .

# 응답 예:
# {
#   "SKU-1001": 100,
#   "SKU-1002": 50,
#   "SKU-1003": 75
# }
```

#### Step 5-2: 주문 생성 (단순 요청)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "SKU-1001",
    "userId": "user-1",
    "quantity": 2
  }' | jq .

# 응답 예:
# {
#   "orderId": "550e8400-e29b-41d4-a716-446655440000",
#   "status": "ACCEPTED",
#   "productId": "SKU-1001",
#   "quantity": 2,
#   "remainingStock": 98,
#   "userId": "user-1"
# }
```

#### Step 5-3: 동시 요청 테스트 (부하 테스트)

```bash
# 10개의 동시 요청
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "{
      \"productId\": \"SKU-1001\",
      \"userId\": \"user-$i\",
      \"quantity\": 1
    }" &
done
wait

# 재고 확인 (90개 남아야 함)
curl http://localhost:8080/api/inventory | jq '.["SKU-1001"]'
```

#### Step 5-4: 재고 부족 테스트

```bash
# 재고가 부족한 경우
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "SKU-1001",
    "userId": "user-999",
    "quantity": 1000
  }' | jq .

# 응답 예 (에러):
# {
#   "timestamp": "2026-08-13T...",
#   "status": 409,
#   "error": "Inventory error",
#   "message": "Not enough stock for product: SKU-1001"
# }
```

### Phase 6: 데이터베이스 확인

#### Step 6-1: MySQL 접속

```bash
mysql -h localhost -u root -ppassword flashsale_db
```

#### Step 6-2: 데이터 조회

```sql
-- 상품 확인
SELECT * FROM products;

-- 주문 확인
SELECT * FROM orders;

-- 재고 확인
SELECT * FROM inventory;

-- 결제 정보 확인
SELECT * FROM payments;

-- 사용자별 주문 조회
SELECT * FROM orders WHERE user_id = 'user-1';

-- 특정 상태의 주문 조회
SELECT * FROM orders WHERE status = 'QUEUED';
```

#### Step 6-3: MySQL 종료

```bash
EXIT;
```

### Phase 7: Redis 상태 확인

```bash
# Redis CLI 접속
redis-cli

# Redis 명령어
PING                    # 연결 확인 (PONG 응답)
KEYS *                  # 모든 키 조회
GET inventory:SKU-1001  # 특정 값 조회
HGETALL orders          # 전체 주문 정보 (Hash)
DEL inventory:SKU-1001  # 키 삭제
FLUSHALL               # 모든 데이터 삭제

# Redis CLI 종료
EXIT
```

### Phase 8: Kafka 메시지 확인

```bash
# Kafka 토픽 확인
docker exec flash-sale-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# 토픽에서 메시지 조회
docker exec flash-sale-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  --max-messages 10
```

### Phase 9: 애플리케이션 중지

#### Step 9-1: Spring Boot 중지

```bash
# 터미널에서 Ctrl+C 누르기
```

#### Step 9-2: Docker 서비스 중지

```bash
# 모든 컨테이너 중지
docker-compose down

# 데이터까지 삭제 (주의!)
docker-compose down -v
```

---

## 📊 데이터 흐름

### 1단계: 클라이언트 요청

```
클라이언트
  ↓
POST http://localhost:8080/api/orders
Body: {
  "productId": "SKU-1001",
  "userId": "user-123",
  "quantity": 2
}
```

### 2단계: OrderController 처리

```java
@PostMapping("/orders")
public ResponseEntity<Map<String, Object>> placeOrder(
    @RequestBody OrderRequest request
) {
    // Spring이 JSON → OrderRequest 객체로 변환
    // OrderRequest: {productId: "SKU-1001", userId: "user-123", quantity: 2}
    
    // 비즈니스 로직 호출
    Map<String, Object> response = flashSaleService.placeOrder(request);
    
    // 응답 반환
    return ResponseEntity.ok(response);
}
```

### 3단계: FlashSaleService 처리

```java
public Map<String, Object> placeOrder(OrderRequest request) {
    // 1️⃣ 요청 검증
    if (request.productId() == null || request.productId().isBlank()) {
        throw new IllegalArgumentException("Product id is required");
    }
    
    String productId = request.productId();
    
    // 2️⃣ Redis 분산 잠금 획득
    // withInventoryLock("inventory:SKU-1001", () -> { ... })
    return withInventoryLock(productId, () -> {
        
        // 🔒 Critical Section 시작 (한 스레드/프로세스만 실행)
        
        // 3️⃣ 현재 재고 조회
        int currentStock = inventory.getOrDefault(productId, 0);
        // currentStock = 100
        
        // 4️⃣ 재고 확인
        if (currentStock < request.quantity()) {  // 100 >= 2 (OK)
            throw new IllegalStateException("Not enough stock");
        }
        
        // 5️⃣ 재고 감소
        int remainingStock = currentStock - request.quantity();
        // remainingStock = 100 - 2 = 98
        inventory.put(productId, remainingStock);
        
        // 6️⃣ Kafka 메시지 발행
        if (orderEventProducer != null) {
            orderEventProducer.publishOrderCreated(
                Map.of(
                    "orderId", "550e8400-...",
                    "productId", "SKU-1001",
                    "userId", "user-123",
                    "quantity", 2,
                    "status", "QUEUED"
                )
            );
        }
        
        // 🔒 Critical Section 끝 (다음 스레드 허용)
        
        // 7️⃣ 응답 생성
        return Map.of(
            "orderId", "550e8400-...",
            "status", "ACCEPTED",
            "productId", "SKU-1001",
            "quantity", 2,
            "remainingStock", 98,
            "userId", "user-123"
        );
    });
}
```

### 4단계: Redis 분산 잠금

```
Thread 1 (Server A)        Thread 2 (Server B)        Thread 3 (Server C)
   ↓                           ↓                           ↓
LOCK 요청                    LOCK 요청                    LOCK 요청
   ↓                           ↓                           ↓
✅ LOCK 획득                  ⏳ 대기 중                    ⏳ 대기 중
   ↓
재고 업데이트
(재고: 100 → 98)
   ↓
LOCK 해제
   ↓
                         ✅ LOCK 획득
                            ↓
                         재고 업데이트
                         (재고: 98 → 96)
                            ↓
                         LOCK 해제
                            ↓
                                                   ✅ LOCK 획득
                                                      ↓
                                                   재고 업데이트
                                                   (재고: 96 → 94)
                                                      ↓
                                                   LOCK 해제
```

### 5단계: Kafka 메시지 발행 (비동기)

```
OrderEventProducer.publishOrderCreated()
  ↓
{
  "orderId": "550e8400-...",
  "productId": "SKU-1001",
  "userId": "user-123",
  "quantity": 2,
  "status": "QUEUED"
}
  ↓
Kafka Broker (Topic: order-events)
  ↓ (저장됨, 내구성 보장)
```

### 6단계: Kafka 메시지 소비 (Worker)

```
OrderEventConsumer (여러 Worker에서 병렬 실행)
  ↓
@KafkaListener가 메시지 수신
  ↓
JSON 역직렬화
  ↓
주문 처리 로직 실행
  ↓
Redis 잠금으로 동시성 제어
  ↓
데이터베이스 업데이트
  ↓
ACK 전송 (메시지 처리 완료)
```

### 7단계: 응답 반환

```java
// OrderController에서 클라이언트로 응답
ResponseEntity.ok({
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "ACCEPTED",
    "productId": "SKU-1001",
    "quantity": 2,
    "remainingStock": 98,
    "userId": "user-123"
})
  ↓
HTTP 200 OK
Content-Type: application/json
  ↓
{
  "orderId": "550e8400-...",
  "status": "ACCEPTED",
  "productId": "SKU-1001",
  "quantity": 2,
  "remainingStock": 98,
  "userId": "user-123"
}
```

### 8단계: 데이터베이스 저장

```sql
-- OrderEventConsumer에서 처리
INSERT INTO orders (
    order_id, user_id, product_id, quantity, 
    total_price, status, created_at, updated_at
) VALUES (
    '550e8400-...', 'user-123', 'SKU-1001', 2, 
    '0.00', 'QUEUED', NOW(), NOW()
);

-- 재고 업데이트
UPDATE inventory 
SET reserved_quantity = reserved_quantity + 2
WHERE product_id = 'SKU-1001';
```

---

## 🎯 요약: 프로젝트 이해의 핵심

### Multi-Tier Architecture
```
Controller (HTTP)
    ↓
Service (비즈니스 로직)
    ↓
Repository (데이터 접근)
    ↓
Database (영구 저장)
```

### Multi-Thread
```
ExecutorService (10개 스레드)
    ├─ PaymentService (결제 비동기)
    ├─ OrderEventConsumer (Kafka 병렬 처리)
    └─ Thread Pool (동시 요청 처리)

동시성 제어:
    ├─ ConcurrentHashMap (메모리)
    ├─ Redis Lock (분산 시스템)
    └─ synchronized (단일 서버)
```

### Distributed System
```
Redis (공유 캐시 & 분산 잠금)
    ↓
Kafka (메시지 큐 & 내구성)
    ↓
MySQL (중앙 데이터베이스)
```

### 실행 순서
```
1. docker-compose up -d     (서비스 시작)
2. ./gradlew test           (테스트 실행)
3. ./gradlew bootRun        (앱 시작)
4. curl API                 (요청 테스트)
5. docker-compose down      (서비스 중지)
```

이제 이 프로젝트의 모든 것을 이해하셨을 겁니다! 🎉
