# Architecture

Order Service calls Payment Service.

Payment Service publishes PaymentCompleted event.

Inventory Service consumes PaymentCompleted event.