FROM confluentinc/cp-kafka-connect:7.8.0

USER root

RUN confluent-hub install --no-prompt mongodb/kafka-connect-mongodb:latest

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest

USER appuser