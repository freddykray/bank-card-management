.PHONY: dev up down restart logs logs-app logs-db test clean build run

dev:
	docker compose up --build

up:
	docker compose up -d

down:
	docker compose down

restart:
	docker compose down
	docker compose up --build

logs:
	docker compose logs -f

logs-app:
	docker compose logs -f app

logs-db:
	docker compose logs -f postgres

test:
	./mvnw test

clean:
	./mvnw clean

build:
	./mvnw clean package

run:
	./mvnw spring-boot:run