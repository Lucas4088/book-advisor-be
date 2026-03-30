FROM confluentinc/cp-kafka-connect:7.8.0

USER root

RUN confluent-hub install --no-prompt mongodb/kafka-connect-mongodb:latest

USER appuser