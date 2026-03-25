# 📘 Notification Consumer – README

## 🚀 Overview

This project is a **Kafka-based Notification Consumer** built using **Spring Boot + MySQL**.

It dynamically processes events and sends **SMS / Email notifications** based on:

* Kafka message headers
* Database-driven configuration
* Dynamic template resolution

---

## 🏗️ Architecture

```
Kafka Topic
    ↓
Consumer (Dynamic Threads from DB)
    ↓
NotificationService
    ↓
TemplateService
    ↓
MySQL (template_master / routing_key_config)
    ↓
Build NotificationRequest
    ↓
SMS / Email Processing
```

---

## ⚙️ Key Features

* ✅ Dynamic Kafka consumer scaling using DB (`NO_OF_CONSUMER`)
* ✅ Single consumer group with configurable concurrency
* ✅ Template-driven notification system
* ✅ Supports **SMS / EMAIL / BOTH**
* ✅ Dynamic payload parsing (no fixed structure)
* ✅ Recursive JSON search for field extraction
* ✅ Centralized configuration via database
* ✅ Error handling for missing payload fields

---

## 🗄️ Database Tables

### 0. `app_config`

Used for dynamic configurations.

| Column       | Description                |
| ------------ | -------------------------- |
| config_key   | Key (e.g., NO_OF_CONSUMER) |
| config_value | Value                      |
| is_active    | Active flag                |

---
### 2. `routing_key_config`

Stores notification templates.

| Column                   | Description                 |
| ------------------------ | --------------------------- |
| template_identifiers     | JSON (eventType, alertType) |
| is_active                | Active flag                 |
| config_name              | varchar                     |

---
### 2. `template_master`

Stores notification templates.

| Column                   | Description                 |
| ------------------------ | --------------------------- |
| message_type             | SMS / EMAIL                 |
| template_identifiers_ref | JSON (eventType, alertType) |
| template_parameters      | JSON (template content)     |
| is_active                | Active flag                 |

---

## 📨 Template Structure

### 📱 SMS Template

```json
{
  "from": "EASTWEST",
  "message": "Hello {customer_name}",
  "template": "DEFAULT",
  "callbackUrl": "...",
  "referenceId": "",
  "mobileNumber": "",
  "templateParams": ["customer_name", "timestamp"]
}
```

---

### 📧 Email Template

```json
{
  "from": "noreply@email.com",
  "to": "",
  "subject": "Notification",
  "body": "Hello {customer_name}",
  "template": "EMAIL_VERIFICATION",
  "templateParams": ["customer_name", "timestamp"]
}
```

---

## 🔍 Template Resolution Logic

Templates are selected based on:

```
eventType + alertType
```

Output:

```
Map<messageType, TemplateMaster>
```

Example:

```
SMS   → Template
EMAIL → Template
```

---

## 🧠 Payload Handling

* Payload is **dynamic & nested**
* No fixed structure required
* Uses **recursive JSON search** to find keys

Example:

```
customer_name → found anywhere in payload
timestamp     → found anywhere in payload
```

---

## ⚠️ Validation Rules

* If required field missing:

  ```
  ❌ Data corrupted: Missing field -> key
  ```
* Mandatory fields:

  * SMS → `mobileNumber`
  * EMAIL → `to`

---

## 📦 Final Output

### NotificationRequest

```json
{
  "type": "BOTH",
  "sms": { ... },
  "email": { ... },
  "templateParams": {
    "customer_name": "Ajai",
    "timestamp": "..."
  }
}
```

---

## 🔄 Kafka Configuration

* Single consumer group: `notification-cg`
* Concurrency controlled via DB:

  ```
  NO_OF_CONSUMER = 3
  ```

---

## 🧪 Running the Project

### 1. Start Dependencies

* Kafka (Docker / Local)
* MySQL

---

### 2. Configure DB

Scripts available in scripts folder

---

### 3. Run Application

```bash
mvn spring-boot:run
```

---

### 4. Produce Kafka Message

Send event with headers:

```
event-type
alert-type
MessageType (SMS / EMAIL / BOTH)
```

---

## 📌 Message Flow

```
Kafka Event
   ↓
Header Extraction
   ↓
Template Lookup
   ↓
Payload Mapping
   ↓
Build NotificationRequest
   ↓
Send SMS / Email
```

---

## ⚡ Future Enhancements

* 🔄 Cache templates (avoid DB calls per message)
* ⚡ Optimize payload lookup (avoid recursion)
* 🌐 Add REST API for config updates
* 📊 Monitoring & metrics
* 🔁 Retry & DLQ handling

---

## 👨‍💻 Tech Stack

* Java 17+
* Spring Boot
* Spring Kafka
* MySQL
* Jackson (JSON processing)

---

## ✅ Summary

This system provides:

* Dynamic, scalable Kafka consumption
* Fully configurable notification routing
* Template-based message generation
* Flexible payload handling
* Production-ready extensibility

---

## 📞 Support

For enhancements or optimization:

* Add caching
* Improve performance
* Extend channels (Push / WhatsApp)

---

**End of README**
