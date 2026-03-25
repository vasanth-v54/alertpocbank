📘 Notification Producer Service
📌 Overview

This service is a Scheduler-based Kafka Producer built using:

Java 21
Spring Boot
H2 Database

It works by polling the database every 10 seconds, picking up pending events, publishing them to Kafka, and updating their status.

🔁 How It Works
Scheduler runs every 10 seconds
Fetch records with status = PENDING from H2
Publish each record to Kafka topic
Update status to PUBLISHED

🏗️ Tech Stack
Java 21
Spring Boot
Apache Kafka
H2 (In-memory DB)
Maven

⚙️ Kafka Details
Topic: notifications.events
Broker: localhost:9092
Partitions: 8

🗄️ Database Details

Scripts available in scripts folder

⏱️ Scheduler
@Scheduled(fixedDelay = 10000)
public void processPendingEvents() {
    // fetch PENDING records
    // send to Kafka
    // update status to PUBLISHED
}
📤 Event Flow
H2 (PENDING) → Scheduler → Kafka → Update as PUBLISHED
🧪 Testing

2. Verify in Kafka
kafka-console-consumer \
  --topic notifications.events \
  --bootstrap-server localhost:9092 \
  --from-beginning
  


✅ Status should be PUBLISHED

⚠️ Notes
This is a POC implementation
Uses polling (not real-time streaming)
Ensures at-least-once delivery
No retry or DLQ yet
🚧 Future Improvements
Add retry mechanism
Add FAILED status
Introduce DLQ
Move to PostgreSQL
Implement Outbox Pattern