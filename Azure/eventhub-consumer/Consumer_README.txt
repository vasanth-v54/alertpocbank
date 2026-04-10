📘 Event Hub Consumer — Full Setup Guide
📌 1. Overview

This service reads pending records from database and publishes them to:

Azure Event Hub → notifications.events

It runs as a scheduled polling producer.

🏗️ 2. Architecture
H2 DB (payload_mst - PENDING)
        ↓
Scheduler (every 10 sec)
        ↓
EventHubProducerClient
        ↓
Azure Event Hub
        ↓
Update status → SENT
⚙️ 3. Tech Stack
Java 17
Spring Boot 3.2.x
Spring Data JPA
H2 Database (POC)
Azure Event Hubs SDK
📦 4. Dependencies (pom.xml)
<dependencies>

    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- H2 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
    </dependency>

    <!-- Azure Event Hub -->
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-messaging-eventhubs</artifactId>
        <version>5.17.0</version>
    </dependency>

</dependencies>
⚙️ 5. Configuration (application.properties)
server.port=8080

# H2 DB
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Event Hub
azure.eventhub.connection-string=Endpoint=sb://<namespace>.servicebus.windows.net/;SharedAccessKeyName=<key>;SharedAccessKey=<value>
azure.eventhub.name=notifications.events
🗄️ 6. Database Design
Table: payload_mst
CREATE TABLE payload_mst (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payload CLOB,
    status VARCHAR(20)
);
📥 Sample Data
INSERT INTO payload_mst (payload, status)
VALUES ('{"eventType":"ACCOUNT_CREATED","id":101}', 'PENDING');
🧱 7. Entity Class
@Entity
@Table(name = "payload_mst")
public class Payload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "CLOB")
    private String payload;

    private String status;

    // getters & setters
}
🗂️ 8. Repository
@Repository
public interface PayloadRepository extends JpaRepository<Payload, Long> {

    List<Payload> findByStatus(String status);
}
📤 9. Event Hub Producer Config
@Configuration
public class EventHubProducerConfig {

    @Value("${azure.eventhub.connection-string}")
    private String connectionString;

    @Value("${azure.eventhub.name}")
    private String eventHubName;

    @Bean
    public EventHubProducerClient producerClient() {
        return new EventHubClientBuilder()
                .connectionString(connectionString, eventHubName)
                .buildProducerClient();
    }
}
🔄 10. Scheduler Service (CORE LOGIC)
@Service
public class EventPublisherService {

    @Autowired
    private PayloadRepository repository;

    @Autowired
    private EventHubProducerClient producerClient;

    @Scheduled(fixedRate = 10000)
    public void publishEvents() {

        List<Payload> pending = repository.findByStatus("PENDING");

        System.out.println("Pending records: " + pending.size());

        for (Payload p : pending) {

            EventDataBatch batch = producerClient.createBatch();

            batch.tryAdd(new EventData(p.getPayload()));

            producerClient.send(batch);

            // update status
            p.setStatus("SENT");
            repository.save(p);

            System.out.println("✅ Sent: " + p.getPayload());
        }
    }
}
⏱️ 11. Enable Scheduling
@SpringBootApplication
@EnableScheduling
public class EventhubProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventhubProducerApplication.class, args);
    }
}
▶️ 12. Run Application
mvn spring-boot:run
🔍 13. Verification
✅ H2 Console

Open:

http://localhost:8080/h2-console

Use:

JDBC URL: jdbc:h2:mem:testdb
User: sa
Password: (empty)
✅ Check Data
SELECT * FROM payload_mst;
Expected:
id	payload	status
1	{...}	SENT
✅ Azure Verification

Go to:

👉 Microsoft Azure Portal

Check:

Event Hub → notifications.events → Metrics

Look for:

Incoming Messages ↑
Incoming Requests ↑
🧠 14. Key Concepts
Concept	Meaning
Scheduler	Poll DB every 10 sec
PENDING	Not yet sent
SENT	Successfully published
Event Hub	Streaming system (Kafka equivalent)
⚠️ 15. Common Issues
❌ No events in Azure

✔ Check:

Connection string
Event hub name
Scheduler running
❌ Records not updating

✔ Check:

JPA config
Transaction/save()
❌ Duplicate events

✔ Add:

status check
idempotency logic
🚀 16. Enhancements (Next Level)
Retry mechanism
Dead Letter Queue
Batch sending optimization
Partition key support
JSON validation
✅ FINAL STATUS
Component			Status
DB polling			✅
Scheduler			✅
Event publishing	✅
Azure integration	✅