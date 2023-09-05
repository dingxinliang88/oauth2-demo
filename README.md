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
├── LICENSE
├── README.md
├── canal-service
├── common
├── doc
│   ├── api-test # Postman json data
│   ├── oauth2  # OAuth2 document
│   └── sql # table sql
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

## Suggest
If you are learning Oauth2, here is a good choice.

I suggest you read the document first. [Click here](./doc/oauth2/OAuth2-Details.md) to read 
the Oauth2 Details. [Click here](./doc/oauth2/OAuth2-Demo.md) to start your OAuth2 tour