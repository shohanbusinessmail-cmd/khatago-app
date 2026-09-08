# Data model

## `financial_records`

| Field | Meaning |
|---|---|
| `id` | UUID primary key |
| `type` | SHOP_CREDIT, LOAN, EMI, BORROWED, LENT, INCOME or EXPENSE |
| `title` | Human-readable record/product description |
| `counterparty` | Shop, institution, person, merchant or income source |
| `category` | User-entered category |
| `amountMinor` | Original amount in integer currency minor units |
| `dateEpochDay` | User-facing record date |
| `dueDateEpochDay` | Optional due/maturity date |
| `installmentAmountMinor` / `installmentCount` | Optional manual plan terms for loans/EMIs |
| `notes`, `paymentMethod`, `phone` | Optional context |
| `createdAt`, `updatedAt` | Audit timestamps |

Indexed fields include type, date, due date, title and counterparty.

## `payments`

Payment rows have their own UUID and reference a record with a foreign key (`ON DELETE CASCADE`). They contain the amount, payment date, method, reference, note and creation timestamp. Payments are only accepted for obligation types. The current paid total is the sum of payment rows, not a mutable counter on the record.

## Derived states

The domain calculator derives `ACTIVE`, `UPCOMING`, `DUE_TODAY`, `DUE_SOON`, `PARTIALLY_PAID`, `PAID`, `OVERDUE` or `COMPLETED` from the current date and ledger totals. A lent record contributes to “owed to me”; shop, loan, EMI and borrowed records contribute to “outstanding”. Income and expense records contribute to cash flow and never get debt statuses.

## Settings

DataStore stores profile name, ISO-like currency code, onboarding completion and notification preference. This keeps small preferences out of Room and allows settings to update without rewriting financial history.

## Backup format

The JSON root contains `format: khatago-backup`, `schemaVersion`, `exportedAt`, `settings`, `records` and `payments`. Version 1 requires valid record/payment ids, known record types, positive amounts, valid payment references and payment totals that do not exceed obligation amounts. Import replacement occurs inside a Room transaction after complete validation.
