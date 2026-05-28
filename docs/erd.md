# SafePill ERD

```mermaid
erDiagram
    users {
        bigint id PK
        varchar login_id
        varchar password
        varchar username
        varchar email
        varchar social_id
        enum provider "Local, Kakao, Google"
        enum gender "MALE, FEMALE"
        date birth_date
        timestamp created_at
        timestamp modified_at
    }

    health_profile {
        bigint id PK
        bigint user_id FK "unique"
        text disease
        text allergy
        json custom_guide
    }

    notification_setting {
        bigint id PK
        bigint user_id FK "unique"
        boolean all_alarm_enabled
        boolean sound_vibrate_enabled
        boolean refill_alarm_enabled
        int snooze_minutes
        time morning_time
        time lunch_time
        time dinner_time
        time night_time
    }

    medicine_master {
        bigint id PK
        varchar item_seq UK
        varchar medicine_name
        varchar medicine_manufacturer
        json appearance_info
        text efficacy
        text use_method
        text precautions
    }

    supplement_master {
        bigint id PK
        varchar item_seq UK
        varchar supplement_name
        varchar supplement_manufacturer
        json appearance_info
        text efficacy
        text intake_method
        text precautions
    }

    ingredient_master {
        bigint id PK
        varchar ingredient_name
        numeric upper_limit
        varchar unit
        varchar best_time_guide
        varchar intake_tip
    }

    medicine_ingredient {
        bigint id PK
        bigint medicine_id FK
        bigint ingredient_id FK
        numeric dosage
    }

    supplement_ingredient {
        bigint id PK
        bigint supplement_id FK
        bigint ingredient_id FK
        numeric dosage
    }

    interaction_rule {
        bigint id PK
        bigint ingredient_a_id FK
        bigint ingredient_b_id FK
        enum risk_level "CAUTION, WARNING, DANGER"
        text description
    }

    user_medication_reg {
        bigint id PK
        bigint user_id FK
        enum item_type "MEDICINE, SUPPLEMENT"
        bigint item_id "logical polymorphic reference"
        int supply_days
    }

    intake_schedule {
        bigint id PK
        bigint reg_id FK
        varchar time_slot
        boolean is_alarm_on
        timestamp created_at
        timestamp modified_at
    }

    intake_log {
        bigint id PK
        bigint schedule_id FK
        timestamp actual_time
        enum status "TAKEN, SKIPPED"
        timestamp created_at
        timestamp modified_at
    }

    chat_session {
        bigint id PK
        bigint user_id FK
        timestamp started_at
        timestamp created_at
        timestamp modified_at
    }

    chat_message {
        bigint id PK
        bigint session_id FK
        text contents
        enum sender_role "User, System"
        timestamp created_at
        timestamp modified_at
    }

    users ||--|| health_profile : has
    users ||--|| notification_setting : configures
    users ||--o{ user_medication_reg : owns
    users ||--o{ chat_session : starts
    medicine_master ||--o{ medicine_ingredient : contains
    supplement_master ||--o{ supplement_ingredient : contains
    ingredient_master ||--o{ medicine_ingredient : maps
    ingredient_master ||--o{ supplement_ingredient : maps
    ingredient_master ||--o{ interaction_rule : ingredient_a
    ingredient_master ||--o{ interaction_rule : ingredient_b
    user_medication_reg ||--o{ intake_schedule : schedules
    intake_schedule ||--o{ intake_log : logs
    chat_session ||--o{ chat_message : messages
```

`user_medication_reg.item_id` is intentionally not a physical FK. It is a polymorphic reference: when `item_type = MEDICINE`, it points to `medicine_master.id`; when `item_type = SUPPLEMENT`, it points to `supplement_master.id`.
