# Woot Clone (Flash Sale System)

A full-stack, enterprise-grade Flash Sale application designed to mimic the aesthetics and functionality of Woot.com. It is built to handle high concurrency using a modern tech stack.

## Tech Stack

### Frontend
- **React.js & Vite**: Lightning fast frontend build tool and rendering.
- **Vanilla CSS**: Custom styling to match Woot.com's signature design (Glassmorphism originally, shifted to flat design with bold typography).
- **Axios & React Router**: For API requests and page navigation.

### Backend
- **Java & Spring Boot**: Core business logic and RESTful APIs.
- **MySQL (via Docker)**: Primary database for persistent storage (Products, Orders, Inventory).
- **Redis (via Docker)**: In-memory datastore acting as a distributed lock to prevent overselling during high-traffic flash sales.
- **Apache Kafka (via Docker)**: Asynchronous event messaging system. Order requests are dumped into a Kafka topic for processing so the web threads are never blocked.
- **Spring Data JPA**: For database interactions.

## How to Run Locally

### 1. Start the Infrastructure (Docker)
Ensure Docker is running, then start MySQL, Redis, and Kafka:
```bash
docker-compose up -d
```

### 2. Start the Backend
From the root directory (`Java_Multithread`), start the Spring Boot server:
```bash
./gradlew bootRun
```
*The backend will automatically seed the MySQL database with realistic Woot items on startup! It runs on `http://localhost:8081`.*

### 3. Start the Frontend
In a new terminal, navigate to the `frontend` folder and start Vite:
```bash
cd frontend
npm install
npm run dev
```
*The frontend will run on `http://localhost:5173`.*

## Features
- **User Portal**: View "Daily Deals", add to cart, and checkout.
- **Admin Dashboard**: Live view of remaining inventory and recent orders. Admins can delete orders to free up stock.
- **High Concurrency Readiness**: The Redis-Lock + Kafka Queue architecture guarantees zero overselling (race conditions) and 100% responsiveness even with thousands of concurrent clicks.
