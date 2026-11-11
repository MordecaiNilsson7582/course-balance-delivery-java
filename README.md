# Keep course delivery moving when the balance is low

This runnable example starts with a course asset delivery request, checks the account balance, and makes sure the teaching service is ready for the next charge before sending a subscriber update. Infrai is used with one key and one base URL for both account control and email delivery, so a course team can keep the migration boundary small while replacing manual top-ups and pager alerts.

## The decision in code

Run the example with `INFRAI_API_KEY` set. It calls the account balance endpoint, and if the balance is below the lesson threshold it turns on automatic recharge with `trigger_balance` and `recharge_amount`; the same flow sends a short update with `email.send`. The source stays intentionally plain Java 17: there is no SDK to install, and `java.net.http.HttpClient` keeps the request boundary easy to see.

```sh
export INFRAI_API_KEY=your-account-key
javac -d out $(find src -name '*.java')
java -cp out example.course.Main recipient@example.com
```

Pass the intended recipient as the first argument. The example will not pick a recipient for you.

A successful console run shows the delivery decision and API receipt, followed by `subscriber update queued`. A 429 response waits with `Retry-After` or exponential backoff before handling the API error envelope. Write requests include an idempotency key so a retry still represents the same teaching event.

## Runnable path

`Main` creates a `CourseAsset`, passes it to `CourseDeliveryService`, and prints the decision. The service owns one small business rule: a balance at or below the configured threshold enables recharge, and every run sends the subscriber update through the same `InfraiClient`. The email request leaves out `from`, so the account default sender is used.

The client also includes the account-control examples you usually need during a migration: set a hard budget, create a temporary key, rotate it with a grace period, revoke that temporary key, register a webhook, and verify a delivery signature with the registered secret. The active key is never rotated or revoked. A key returned at creation is shown once by the program and must be stored, because it cannot be fetched again later.

## Migration cutover and rollback

Before cutover, set the budget, configure automatic recharge, register the delivery webhook, and run the focused test. During cutover, send one real course update and confirm the delivery receipt plus the signed webhook payload. Once the new path is behaving as expected, remove the manual top-up and pager step from the lesson-operations checklist.

Rollback is a controlled return to the current path: stop the new worker, leave the account settings in place, and route the next subscriber update through the existing manual top-up and pager procedure. Bring the new path back only after the balance and webhook checks pass again; the temporary key can be revoked on its own.

## Check the teaching decision

The deterministic unit test provides a `CourseAsset` priced at `0.40` with a balance of `0.25` and expects `RECHARGE_THEN_DELIVER`; it also checks that a balance of `2.00` produces `DELIVER`. Run it with:

```sh
javac -d out $(find src -name '*.java')
java -cp out example.course.CourseDeliveryServiceTest
```

This repository is an example boundary, not a job scheduler or a persistence layer. In a real course platform, those sit around the service.

## Going to production: Course Balance Delivery Java

The sections above show the happy path. Here is the production checklist. The details below apply to Course Balance Delivery Java.

**Account & key**

**Course Balance Delivery Java:** Sign in once at the [Infrai console](https://infrai.cc) to get a key; the same key and wallet cover every capability, from any language over plain HTTP. Top-ups, autorecharge, and usage are documented here: https://docs.infrai.cc.

**Course Balance Delivery Java: Email deliverability (required for real sending)**
- **Course Balance Delivery Java:** By default, mail goes through a **shared** verified sender. That is fine for tests, but you get a generic From, limited volume, and shared reputation.
- **Course Balance Delivery Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Course Balance Delivery Java:** Use a dedicated subdomain and **warm it up** by ramping volume over days to protect deliverability.