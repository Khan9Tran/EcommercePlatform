# 🛒 Ecommerce Platform - Backend

Đây là dự án backend cho hệ thống thương mại điện tử, sử dụng **Spring Boot** và các công nghệ hiện đại như RabbitMQ, Docker, JWT...

---

## 📂 Cấu trúc thư mục

com.hkteam.ecommerce_platform
├── configuration # Cấu hình Spring (CORS, Swagger, Security,...)
├── constant # Các hằng số dùng chung
├── controller # REST API controllers (User, Product, Order,...)
├── dto # DTO (Data Transfer Object)
├── entity # Các entity tương ứng với bảng DB
├── enums # Các kiểu enum (Role, OrderStatus,...)
├── exception # Xử lý exception toàn cục và custom
├── mapper # MapStruct để chuyển đổi giữa DTO và Entity
├── rabbitmq # Cấu hình và sử dụng RabbitMQ
├── repository # Spring Data JPA repositories
├── service # Chứa business logic
├── util # Các hàm tiện ích (Utils)
├── validator # Custom validators (VD: Email, Phone,...)
└── EcommercePlatformApplication.java # Điểm bắt đầu chạy ứng dụng


---

## 🧰 Công nghệ sử dụng

- Java 22+
- Spring Boot
- Spring Data JPA
- RabbitMQ
- PostgreSQL
- Docker & Docker Compose
- MapStruct
- Lombok
- JWT (JSON Web Token)
- Redis
- Auth2.0
- Elasticsearch
---

## ⚙️ Cài đặt và chạy dự án

### 1. Yêu cầu

- Java 22+
- Docker + Docker Compose
- Maven

### 2. Cấu hình `.env`

Tạo file `.env` ở thư mục gốc (nếu chưa có) để cấu hình các biến môi trường.

### 3. Chạy bằng Docker

```bash
docker-compose up --build
