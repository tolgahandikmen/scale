# scale-backend

Java 8 + Spring Framework backend scaffold for the SCALE frontend.

## Architecture
- `ScaleWS` receives HTTP API calls
- `ScaleService` routes business logic
- DAO classes perform JDBC operations and DB persistence
- DTO classes carry request/response payloads

## Endpoints
All endpoints are under `/scale`:
- `POST /createType`
- `GET /items/tree`
- `GET /items/{itemId}`
- `GET /items/partIds`
- `POST /sheets`
- `GET /sheets?itemId=...&kind=...`
- `GET /sheets/{sheetId}`
- `GET /sheets/{inputSheetId}/outputs`
- `GET /templates?kind=INPUT|OUTPUT`
- `GET /templates/{templateId}/fields`
- `POST /templates/version`
- `POST /templates/fields/version`
- `GET /part-template-mappings`
- `GET /part-template-mappings/{partId}`
- `POST /part-template-mappings`

## DB Configuration
Set values in `src/main/resources/application.properties`:
- `db.driver`
- `db.url`
- `db.username`
- `db.password`

## Build
```bash
mvn clean package
```
