# Factory Method: In-App Notifications

## What was added
- `NotificationCreator` is the abstract Creator and declares the factory method `create(...)`.
- `InAppNotificationCreator` is the Concrete Creator that creates an in-app `Notification`.
- `NotificationService` persists created notifications.
- `VerificationService` creates a notification for the parent when an administrator approves or rejects a verification request.
- `GET /notifications/me` returns the authenticated user's notifications.
- `PATCH /notifications/{id}/read` marks one of the authenticated user's notifications as read.

## Database migration
The current project uses `spring.jpa.hibernate.ddl-auto=validate` and does not run schema migrations automatically. Apply this SQL once to the Java database before starting the updated backend:

```bash
docker compose -p security-java exec -T postgres sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < databases/migrations/V1__create_notifications.sql
```

This adds only the `notifications` table and its index. It does not drop or rewrite existing tables/data.

## Demo
1. Log in as a parent and submit a verification request.
2. Log in as an administrator and approve or reject the request.
3. Log back in as that parent.
4. Call `GET /notifications/me` in Swagger. A notification should appear.
5. Call `PATCH /notifications/{notificationId}/read` to mark it as read.

The parent must use their own JWT. The endpoint returns only notifications belonging to the authenticated user.
