# FishSeller Codebase Guide for AI Agents

## Project Overview
**FishSeller** is a Spring Boot 3.5.8 e-commerce REST API for fish retail. It uses JWT authentication, MySQL database, and follows a layered architecture (Controller → Service → Repository → Entity).

## Key Technologies
- **Framework**: Spring Boot 3.5.8 (Java 21)
- **Database**: MySQL with Hibernate/JPA
- **Auth**: JWT tokens (io.jsonwebtoken 0.11.5) + Spring Security + BCrypt
- **Additional**: Lombok, Apache POI, Spring Mail, Spring Scheduling
- **Build**: Maven

## Architecture Pattern

### Layered Structure
```
controller/ → service/ → repository/ → entity/
      ↓            ↓            ↓
   REST API    Business    DB Queries    Persistence
              Logic
       ↓
      dto/  (Data Transfer Objects - @SuperBuilder with BaseDTO)
```

### Key Conventions

**Base Classes** (Entity & DTO inheritance):
- [BaseEntity](src/main/java/datn/duong/FishSeller/entity/BaseEntity.java): `@MappedSuperclass`, uses `@SuperBuilder` (not @Builder) for inheritance support, auto-timestamps via `@CreationTimestamp/@UpdateTimestamp`
- [BaseDTO](src/main/java/datn/duong/FishSeller/dto/BaseDTO.java): Uses `@SuperBuilder` for inheritance, JSON date format: `dd-MM-yyyy HH:mm:ss`, timezone: `Asia/Ho_Chi_Minh`

**Service Layer**:
- All services use `@Service` + `@RequiredArgsConstructor` (Lombok)
- Use `@Transactional(readOnly = true)` for read-only operations
- Services access current user via `userService.getCurrentProfile()` (extracts from JWT context)

**Repository Layer**:
- Extend `JpaRepository<Entity, Long>`
- All marked with `@Repository`

**Controllers**:
- Prefix with REST mapping: `@RequestMapping("/resource")`
- Use `ResponseEntity<DTO>` return types
- All endpoints under `/api/v1.0/` (context-path in [application.properties](src/main/resources/application.properties))

### Security & Authentication

**JWT Flow**:
1. [JwtRequestFilter](src/main/java/datn/duong/FishSeller/security/JwtRequestFilter.java): Extracts token from `Authorization: Bearer <token>` header
2. [JwtUtil](src/main/java/datn/duong/FishSeller/util/JwtUtil.java): Creates/validates tokens (secret in properties)
3. [SecurityConfig](src/main/java/datn/duong/FishSeller/config/SecurityConfig.java): Stateless session policy, CORS enabled for all origins

**Authorization Rules** (from SecurityConfig):
- Public: `/status`, `/healthCheck`, `/register`, `/activate`, `/login`, `/products/**`, `/categories/**`
- Admin-only: `/admin/**` (requires `ADMIN` authority)
- Authenticated: All other endpoints require valid JWT

**Password**: BCryptPasswordEncoder (configured in SecurityConfig)

## Data Model Patterns

**Entity Relationships**:
- User → Cart (1:1)
- User → Order (1:N)
- Cart → CartItem (1:N)
- Product → CartItem/OrderItem (1:N)
- Category → Product (1:N)

**Important Entities**:
- [UserEntity](src/main/java/datn/duong/FishSeller/entity/UserEntity.java): Extends BaseEntity, contains roles for authentication
- [ProductEntity](src/main/java/datn/duong/FishSeller/entity/ProductEntity.java): References Category
- [CartEntity/OrderEntity](src/main/java/datn/duong/FishSeller/entity/CartEntity.java): Contains collections of items
- [PasswordResetTokenEntity](src/main/java/datn/duong/FishSeller/entity/PasswordResetTokenEntity.java): For account recovery

**DTO Mapping**: Each entity has a corresponding DTO (e.g., ProductEntity → ProductDTO). DTOs extend BaseDTO and use `@SuperBuilder`.

## Developer Workflows

### Build & Run
```bash
# Build
mvn clean install

# Run (via Spring Boot plugin)
mvn spring-boot:run

# Or run the compiled JAR
java -jar target/FishSeller-0.0.1-SNAPSHOT.jar
```

### Database
- **Init**: `spring.jpa.hibernate.ddl-auto=update` (auto-creates/updates schema)
- **Config**: MySQL 8 dialect, timezone: `Asia/Ho_Chi_Minh`
- **Credentials**: See [application.properties](src/main/resources/application.properties) (root/duong19082004 - change in production)

### Email Service
- Used for activation emails and password recovery
- Configured via Gmail SMTP in [application.properties](src/main/resources/application.properties)
- Service: [EmailService](src/main/java/datn/duong/FishSeller/service/EmailService.java)

### Key Services & Responsibilities

| Service | Purpose | Key Methods |
|---------|---------|-------------|
| [CartService](src/main/java/datn/duong/FishSeller/service/CartService.java) | Shopping cart management | getMyCart(), addToCart(), updateItemQuantity() |
| [OrderService](src/main/java/datn/duong/FishSeller/service/OrderService.java) | Order processing | createOrder(), trackOrder() |
| [ProductService](src/main/java/datn/duong/FishSeller/service/ProductService.java) | Product catalog | getAllProducts(), getProductById() |
| [UserService](src/main/java/datn/duong/FishSeller/service/UserService.java) | User management & auth | registerUser(), getCurrentProfile() |
| [CategoryService](src/main/java/datn/duong/FishSeller/service/CategoryService.java) | Category management | getCategories() |

## Common Patterns to Follow

**1. User Context Access**:
```java
UserEntity currentUser = userService.getCurrentProfile();
```
This extracts user from JWT (SecurityContext). Use in cart/order operations.

**2. DTO Conversion**:
```java
@SuperBuilder
public class ProductDTO extends BaseDTO { /* fields */ }
// Use builder pattern: ProductDTO.builder().id(1L).name("Fish").build()
```

**3. Optional Handling**:
```java
entity.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
```

**4. Transactional Operations**:
```java
@Transactional  // For write operations
public void updateCart(CartDTO dto) { /* ... */ }

@Transactional(readOnly = true)  // For read-only operations
public CartDTO getMyCart() { /* ... */ }
```

**5. REST Response Format**:
```java
@PostMapping
public ResponseEntity<CartDTO> addToCart(@RequestBody AddToCartRequest req) {
    return ResponseEntity.ok(cartService.addToCart(req));
}
// URL format: /api/v1.0/cart (note: api/v1.0 prefix added by context-path)
```

## Important Notes

- **Timestamps**: All dates use `LocalDateTime` with `Asia/Ho_Chi_Minh` timezone
- **JSON Format**: Dates serialize as `dd-MM-yyyy HH:mm:ss`
- **Lombok**: Always use `@SuperBuilder` for classes that extend BaseEntity/BaseDTO
- **Testing**: JUnit + Spring Security Test dependencies available
- **Current Port**: 8080 (with context-path `/api/v1.0`)

## Debugging & Exploration

When exploring features:
1. Start from [CartController](src/main/java/datn/duong/FishSeller/controller/CartController.java) - well-documented with inline comments explaining each endpoint
2. Check [application.properties](src/main/resources/application.properties) for config settings (timezone, DB, JWT secret, email)
3. Review [SecurityConfig](src/main/java/datn/duong/FishSeller/config/SecurityConfig.java) for authorization rules
4. Services contain most business logic; repositories handle JPA queries
