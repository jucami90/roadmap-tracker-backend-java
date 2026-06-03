package com.roadmap.app.service;

import com.roadmap.app.model.Task;
import com.roadmap.app.model.Task.Category;
import com.roadmap.app.model.Task.Priority;
import com.roadmap.app.model.Week;
import com.roadmap.app.repository.TaskRepository;
import com.roadmap.app.repository.WeekRepository;

import java.time.LocalDate;
import java.util.List;

public class DataSeeder {

    private final WeekRepository weekRepo = new WeekRepository();
    private final TaskRepository taskRepo = new TaskRepository();

    private static final LocalDate START = LocalDate.of(2026, 6, 3);

    public void seedIfEmpty() {
        if (weekRepo.count() > 0) return;
        System.out.println("[DataSeeder] First launch – seeding 12 weeks...");
        seedAllWeeks();
        System.out.println("[DataSeeder] Done.");
    }

    private void seedAllWeeks() {
        // ── MONTH 1 ─────────────────────────────────────────────
        Week w1 = createWeek(1, "DSA Fundamentals", "Month 1 – Foundations", "#2E7D32",
            "Arrays, Strings, HashMap, Sliding Window, Two Pointer, Prefix Sum",
            "2–3 problems/day: Two Sum, 3 Sum, Longest Substring, Product of Array, Container With Most Water, Subarray Sum Equals K",
            "Solve 2–3 problems daily; understand patterns", "2.5–3 h", "6–8 h");
        seedTasks(w1, new String[][]{
            {"Study Arrays & Strings patterns",    "DSA",          "HIGH"},
            {"Study HashMap techniques",           "DSA",          "HIGH"},
            {"Sliding Window problems",            "DSA",          "MEDIUM"},
            {"Two Pointer problems",               "DSA",          "MEDIUM"},
            {"Prefix Sum problems",                "DSA",          "MEDIUM"},
            {"Solve: Two Sum",                     "DSA",          "HIGH"},
            {"Solve: 3 Sum",                       "DSA",          "HIGH"},
            {"Solve: Longest Substring No Repeat", "DSA",          "HIGH"},
            {"Solve: Product of Array Except Self","DSA",          "HIGH"},
            {"Solve: Container With Most Water",   "DSA",          "HIGH"},
            {"Solve: Subarray Sum Equals K",       "DSA",          "HIGH"},
        });

        Week w2 = createWeek(2, "Java Core", "Month 1 – Foundations", "#2E7D32",
            "OOP, Collections, Generics, Comparable vs Comparator, Streams API, Optional, Exception Handling, Concurrency Basics",
            "FAQs: HashMap internals, ConcurrentHashMap, ArrayList vs LinkedList, HashSet vs TreeSet, equals()/hashCode(), String vs StringBuilder",
            "Answer Java internals questions confidently", "2.5–3 h", "6–8 h");
        seedTasks(w2, new String[][]{
            {"OOP Concepts deep dive",                  "JAVA", "HIGH"},
            {"Collections Framework study",             "JAVA", "HIGH"},
            {"Generics & type bounds",                  "JAVA", "MEDIUM"},
            {"Comparable vs Comparator practice",       "JAVA", "MEDIUM"},
            {"Streams API exercises",                   "JAVA", "HIGH"},
            {"Optional & Exception Handling",           "JAVA", "MEDIUM"},
            {"Concurrency Basics (threads, sync)",      "JAVA", "HIGH"},
            {"FAQ: HashMap internal working",           "JAVA", "HIGH"},
            {"FAQ: ArrayList vs LinkedList",            "JAVA", "MEDIUM"},
            {"FAQ: equals() vs hashCode()",             "JAVA", "HIGH"},
            {"FAQ: String vs StringBuilder",            "JAVA", "MEDIUM"},
        });

        Week w3 = createWeek(3, "Spring Boot Deep Dive", "Month 1 – Foundations", "#2E7D32",
            "Spring IOC, Dependency Injection, Bean Lifecycle, Spring MVC, REST APIs, Validation, Exception Handling, Profiles, Configuration",
            "Create: User CRUD APIs, Global Exception Handling, Swagger Documentation",
            "Build a working Spring Boot REST application", "2.5–3 h", "6–8 h");
        seedTasks(w3, new String[][]{
            {"Spring IOC & DI concepts",               "SPRING_BOOT", "HIGH"},
            {"Bean Lifecycle & scopes",                "SPRING_BOOT", "HIGH"},
            {"Spring MVC architecture",                "SPRING_BOOT", "HIGH"},
            {"Build User CRUD REST API",               "SPRING_BOOT", "HIGH"},
            {"Add Bean Validation (@Valid)",           "SPRING_BOOT", "MEDIUM"},
            {"Global Exception Handling (@ControllerAdvice)", "SPRING_BOOT", "HIGH"},
            {"Swagger / OpenAPI documentation",        "SPRING_BOOT", "MEDIUM"},
            {"Profiles & application.properties",      "SPRING_BOOT", "LOW"},
        });

        Week w4 = createWeek(4, "Database + SQL", "Month 1 – Foundations", "#2E7D32",
            "Joins, Indexes, Query Optimization, Normalization, Transactions, ACID, Isolation Levels",
            "Practice: Top N records, Duplicate records, Window Functions, Group By",
            "Solve SQL questions without looking at syntax", "2.5–3 h", "6–8 h");
        seedTasks(w4, new String[][]{
            {"Master all JOIN types",               "SQL", "HIGH"},
            {"Indexes & query optimization",        "SQL", "HIGH"},
            {"Normalization (1NF–3NF)",             "SQL", "MEDIUM"},
            {"ACID properties & Transactions",      "SQL", "HIGH"},
            {"Isolation Levels",                    "SQL", "HIGH"},
            {"Practice: Top N records",             "SQL", "HIGH"},
            {"Practice: Find duplicates",           "SQL", "MEDIUM"},
            {"Practice: Window Functions",          "SQL", "HIGH"},
            {"Practice: GROUP BY + HAVING",         "SQL", "MEDIUM"},
        });

        // ── MONTH 2 ─────────────────────────────────────────────
        Week w5 = createWeek(5, "Low Level Design", "Month 2 – System Design", "#1565C0",
            "SOLID Principles, Design Patterns: Factory, Strategy, Builder, Observer, Singleton",
            "Design: Parking Lot, Elevator, Library Management System",
            "Apply SOLID & patterns to real-world design problems", "2.5–3 h", "6–8 h");
        seedTasks(w5, new String[][]{
            {"SOLID Principles study + examples",      "SYSTEM_DESIGN", "HIGH"},
            {"Factory Pattern implementation",         "SYSTEM_DESIGN", "HIGH"},
            {"Strategy Pattern implementation",        "SYSTEM_DESIGN", "HIGH"},
            {"Builder Pattern implementation",         "SYSTEM_DESIGN", "MEDIUM"},
            {"Observer Pattern implementation",        "SYSTEM_DESIGN", "MEDIUM"},
            {"Singleton Pattern implementation",       "SYSTEM_DESIGN", "MEDIUM"},
            {"Design: Parking Lot system",             "SYSTEM_DESIGN", "HIGH"},
            {"Design: Elevator system",                "SYSTEM_DESIGN", "HIGH"},
            {"Design: Library Management system",      "SYSTEM_DESIGN", "HIGH"},
        });

        Week w6 = createWeek(6, "Microservices", "Month 2 – System Design", "#1565C0",
            "Service Discovery, API Gateway, Circuit Breaker, Config Server, Feign Client | Tools: Spring Cloud, Eureka, OpenFeign, Resilience4j",
            "Build: User Service, Order Service, Notification Service",
            "Working multi-service Spring Cloud project on GitHub", "2.5–3 h", "6–8 h");
        seedTasks(w6, new String[][]{
            {"Service Discovery with Eureka",          "SPRING_BOOT", "HIGH"},
            {"API Gateway setup",                      "SPRING_BOOT", "HIGH"},
            {"Circuit Breaker with Resilience4j",      "SPRING_BOOT", "HIGH"},
            {"Config Server setup",                    "SPRING_BOOT", "MEDIUM"},
            {"Feign Client inter-service calls",       "SPRING_BOOT", "HIGH"},
            {"Build User Service",                     "PROJECT",     "HIGH"},
            {"Build Order Service",                    "PROJECT",     "HIGH"},
            {"Build Notification Service",             "PROJECT",     "HIGH"},
            {"Push project to GitHub",                 "PROJECT",     "MEDIUM"},
        });

        Week w7 = createWeek(7, "High Level Design", "Month 2 – System Design", "#1565C0",
            "Load Balancer, Caching, Database Scaling, Message Queue, CAP Theorem",
            "Design: URL Shortener, WhatsApp clone, Notification Service, Food Delivery App",
            "Explain system trade-offs confidently", "2.5–3 h", "6–8 h");
        seedTasks(w7, new String[][]{
            {"Load Balancer strategies",               "SYSTEM_DESIGN", "HIGH"},
            {"Caching strategies (Redis, CDN)",        "SYSTEM_DESIGN", "HIGH"},
            {"Database Scaling (sharding, replication)","SYSTEM_DESIGN","HIGH"},
            {"Message Queue concepts",                 "SYSTEM_DESIGN", "MEDIUM"},
            {"CAP Theorem deep dive",                  "SYSTEM_DESIGN", "HIGH"},
            {"Design: URL Shortener",                  "SYSTEM_DESIGN", "HIGH"},
            {"Design: WhatsApp / messaging app",       "SYSTEM_DESIGN", "HIGH"},
            {"Design: Notification Service",           "SYSTEM_DESIGN", "MEDIUM"},
            {"Design: Food Delivery App",              "SYSTEM_DESIGN", "HIGH"},
        });

        Week w8 = createWeek(8, "Messaging + Caching", "Month 2 – System Design", "#1565C0",
            "Redis, Kafka, RabbitMQ | Understand: Producer/Consumer, Partitions, Consumer Groups, Event Driven Architecture",
            "Add Redis Cache + Kafka Event Publishing to existing project",
            "Integrate async messaging & caching into microservices", "2.5–3 h", "6–8 h");
        seedTasks(w8, new String[][]{
            {"Redis data types & use cases",           "SPRING_BOOT", "HIGH"},
            {"Kafka architecture & concepts",          "SPRING_BOOT", "HIGH"},
            {"Kafka Producer/Consumer implementation", "SPRING_BOOT", "HIGH"},
            {"RabbitMQ basics",                        "SPRING_BOOT", "MEDIUM"},
            {"Consumer Groups & Partitions",           "SYSTEM_DESIGN","HIGH"},
            {"Event Driven Architecture patterns",     "SYSTEM_DESIGN","HIGH"},
            {"Add Redis Cache to project",             "PROJECT",     "HIGH"},
            {"Add Kafka Event Publishing to project",  "PROJECT",     "HIGH"},
        });

        // ── MONTH 3 ─────────────────────────────────────────────
        Week w9 = createWeek(9, "Build Resume-Worthy Project", "Month 3 – Interview Prep", "#6A1B9A",
            "E-commerce Backend: Authentication, Product Service, Cart, Order, Payment Mock, Kafka, Redis, JWT",
            "Deploy with Docker + GitHub; write clean README",
            "Fully deployed e-commerce backend on GitHub", "2.5–3 h", "6–8 h");
        seedTasks(w9, new String[][]{
            {"Authentication module (JWT)",            "PROJECT", "HIGH"},
            {"Product Service",                        "PROJECT", "HIGH"},
            {"Cart Service",                           "PROJECT", "HIGH"},
            {"Order Service",                          "PROJECT", "HIGH"},
            {"Payment Integration Mock",               "PROJECT", "HIGH"},
            {"Kafka Events integration",               "PROJECT", "HIGH"},
            {"Redis Cache integration",                "PROJECT", "HIGH"},
            {"Dockerize the application",              "PROJECT", "HIGH"},
            {"Push to GitHub with README",             "PROJECT", "MEDIUM"},
        });

        Week w10 = createWeek(10, "Mock Interviews", "Month 3 – Interview Prep", "#6A1B9A",
            "DSA: 1 Medium problem/day | Java: 30 common questions | Spring Boot: 30 common questions | System Design: 1 design/day",
            "Record mock answers, review & iterate",
            "Complete 7 DSA + 7 system designs + Java/SB Q&A bank", "2.5–3 h", "6–8 h");
        seedTasks(w10, new String[][]{
            {"DSA Mock Day 1 (1 Medium problem)",      "DSA",         "HIGH"},
            {"DSA Mock Day 2 (1 Medium problem)",      "DSA",         "HIGH"},
            {"DSA Mock Day 3 (1 Medium problem)",      "DSA",         "HIGH"},
            {"DSA Mock Day 4 (1 Medium problem)",      "DSA",         "HIGH"},
            {"DSA Mock Day 5 (1 Medium problem)",      "DSA",         "HIGH"},
            {"Java 30 Q&A – Part 1 (1–15)",           "JAVA",        "HIGH"},
            {"Java 30 Q&A – Part 2 (16–30)",          "JAVA",        "HIGH"},
            {"Spring Boot 30 Q&A – Part 1",           "SPRING_BOOT", "HIGH"},
            {"Spring Boot 30 Q&A – Part 2",           "SPRING_BOOT", "HIGH"},
            {"System Design Mock Day 1",               "SYSTEM_DESIGN","HIGH"},
            {"System Design Mock Day 2",               "SYSTEM_DESIGN","HIGH"},
        });

        Week w11 = createWeek(11, "Resume + Applications", "Month 3 – Interview Prep", "#6A1B9A",
            "Polish 1–2 page resume: performance improvements, scalability, cost optimization, production issues",
            "Apply to 10–15 jobs/day on LinkedIn, Naukri, Instahyre, Foundit",
            "Resume polished; 70–105 applications submitted", "2.5–3 h", "6–8 h");
        seedTasks(w11, new String[][]{
            {"Draft resume v1 (1–2 pages)",              "OTHER",      "HIGH"},
            {"Highlight performance improvements",       "OTHER",      "HIGH"},
            {"Highlight scalability & cost projects",    "OTHER",      "MEDIUM"},
            {"Highlight production issues solved",       "OTHER",      "MEDIUM"},
            {"Optimize LinkedIn profile",                "OTHER",      "HIGH"},
            {"Apply: 10–15 jobs Day 1–2",               "INTERVIEW",  "HIGH"},
            {"Apply: 10–15 jobs Day 3–4",               "INTERVIEW",  "HIGH"},
            {"Apply: 10–15 jobs Day 5–7",               "INTERVIEW",  "HIGH"},
        });

        Week w12 = createWeek(12, "Interview Sprint", "Month 3 – Interview Prep", "#6A1B9A",
            "Daily: 1h DSA + 1h Java & Spring Boot + 1h System Design + 30min Behavioral",
            "Prep: Tell me about yourself, Biggest challenge, Production issue solved, Why changing jobs?",
            "Interview-ready for 5–12 years backend engineer roles", "3–4 h", "6–8 h");
        seedTasks(w12, new String[][]{
            {"'Tell me about yourself' answer",         "INTERVIEW", "HIGH"},
            {"'Biggest challenge' STAR answer",         "INTERVIEW", "HIGH"},
            {"'Production issue solved' story",         "INTERVIEW", "HIGH"},
            {"'Why are you changing jobs?' answer",     "INTERVIEW", "HIGH"},
            {"Final DSA sprint (7 problems)",           "DSA",       "HIGH"},
            {"Final Java internals review",             "JAVA",      "HIGH"},
            {"Final Spring Boot review",                "SPRING_BOOT","HIGH"},
            {"Final System Design practice",            "SYSTEM_DESIGN","HIGH"},
            {"Full mock interview (self-recorded)",     "INTERVIEW", "HIGH"},
        });
    }

    private Week createWeek(int num, String title, String phase, String color,
                             String topics, String practice, String goal,
                             String wdH, String weH) {
        LocalDate ws = START.plusWeeks(num - 1);
        LocalDate we = ws.plusDays(6);
        Week w = new Week(num, title, phase, color, ws, we, topics, practice, goal, wdH, weH);
        return weekRepo.save(w);
    }

    private void seedTasks(Week week, String[][] taskData) {
        for (int i = 0; i < taskData.length; i++) {
            String[] d = taskData[i];
            Category cat = switch (d[1]) {
                case "DSA"           -> Category.DSA;
                case "JAVA"          -> Category.JAVA;
                case "SPRING_BOOT"   -> Category.SPRING_BOOT;
                case "SQL"           -> Category.SQL;
                case "SYSTEM_DESIGN" -> Category.SYSTEM_DESIGN;
                case "PROJECT"       -> Category.PROJECT;
                case "INTERVIEW"     -> Category.INTERVIEW;
                default              -> Category.OTHER;
            };
            Priority pri = switch (d[2]) {
                case "HIGH"   -> Priority.HIGH;
                case "LOW"    -> Priority.LOW;
                default       -> Priority.MEDIUM;
            };
            Task t = new Task(week, d[0], cat, pri);
            t.setOrderIndex(i);
            t.setDueDate(week.getEndDate());
            taskRepo.save(t);
        }
    }
}
