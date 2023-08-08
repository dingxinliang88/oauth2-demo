# OAuth2 Demo

## Feature
- Gateway Filter
- SMS Service
- OAuth2 Service
  - generate token
  - check token
- User Service 
  - bind phone
  - username-password
  - phone-code
  - third-party

## Project Structure
```
.
├── README.md
├── canal-service
├── common
├── doc
│   ├── api-test # postman test 
│   └── sql # database sql
├── gateway-service
├── oauth2-service
├── pom.xml
├── sms-service
└── user-service
```

## Tech Stack
- SpringBoot 2.7.0
- SpringCloud 2021.0.1
- SpringCloud Alibaba 2021.0.1.0
- MySQL 8.0.31
- Nacos 2.2.2
- Canal 1.6.1
- Redis 7.0.5
- RabbitMQ 3.12.0
- OAuth2 (SpringCloud) 2.2.5.RELEASE