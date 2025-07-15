Hello


# Chatgpt plus
Hey there! How can I help you today?








# Tú dijiste:
Home Assignment - Backend Engineer - Service with scheduled REST calls.pdf
PDF
See this PDF. create a prompt that will make an LLM create the software required. Note that it must be in Java, likely I want spring boot frameworks and related integrations for this software, and I'd like it to be docker integrated so I can test and execute it in my machine


# Chatgpt plus
Based on the assignment details, here’s a complete prompt you can use to instruct a large language model (LLM) like ChatGPT or GitHub Copilot to generate the required software in Java with Spring Boot, fully Docker-integrated:

✅ LLM Prompt to Generate the Software
You are a senior Java backend developer.

Create a complete Spring Boot microservice based on the following requirements. The application should be implemented in Java, use Spring Boot, and be Dockerized so it can be easily built, tested, and run locally.

📝 Requirements
Expose a REST endpoint to receive status updates about sports events:

POST /events/status

Accepts a JSON body like:

json
Copiar
Editar
{
  "eventId": "1234",
  "status": "live" // or "not_live"
}
The service should store the live/not-live status of events in an in-memory store (e.g., a concurrent map).

Schedule periodic REST calls:

For each event marked as “live,” schedule a job that fires every 10 seconds.

Each scheduled job should make an HTTP GET request to a simulated external REST API (hardcoded or mock service).

Example API response:

json
Copiar
Editar
{
  "eventId": "1234",
  "currentScore": "0:0"
}
Transform the API response into a message and publish it to a Kafka topic:

Use Spring Kafka for integration.

The message should contain the event ID and score.

Log all publishing attempts and include retry logic for transient Kafka errors.

Error handling & logging:

Handle and log errors for invalid input, failed REST calls, and failed Kafka publishes.

Use SLF4J with Logback for structured logging.

Testing:

Add unit tests and integration tests for:

Event status updates

Scheduled REST calls

Message publishing

Error handling

Deliverables:

Code must include:

A README.md with setup, run, and test instructions.

A Dockerfile and optionally docker-compose.yml to run the application and Kafka.

Comments or documentation about any AI-generated code or design suggestions used.

🧱 Technologies
Java 17+

Spring Boot 3.x

Spring Web

Spring Kafka

Lombok (optional)

SLF4J / Logback

JUnit 5

Mockito

Docker

Let me know if you want me to generate this actual project structure or even the code itself—I can scaffold the Spring Boot app, create the controllers, services, schedulers, Kafka integration, and Docker config.