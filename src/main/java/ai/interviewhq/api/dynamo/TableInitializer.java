package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.config.DynamoDbProperties;
import ai.interviewhq.api.domain.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.util.List;

/**
 * Creates the single table and GSIs when {@code ih.dynamodb.auto-create-table=true}
 * (local/dev only). stg/prod should provision the table out of band.
 */
@Component
public class TableInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TableInitializer.class);

    private final DynamoDbClient client;
    private final DynamoDbProperties properties;

    public TableInitializer(DynamoDbClient client, DynamoDbProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isAutoCreateTable()) {
            log.info("DynamoDB auto-create disabled; expecting table {}", properties.getTable());
            return;
        }
        ensureTable();
    }

    public void ensureTable() {
        String table = properties.getTable();
        try {
            var described = client.describeTable(DescribeTableRequest.builder().tableName(table).build());
            log.info("DynamoDB table {} already exists (status={})", table, described.table().tableStatus());
            waitUntilActive(table);
            return;
        } catch (ResourceNotFoundException ignored) {
            log.info("Creating DynamoDB table {} with GSI1-GSI4", table);
        }

        client.createTable(CreateTableRequest.builder()
                .tableName(table)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .keySchema(
                        key(Keys.PK, KeyType.HASH),
                        key(Keys.SK, KeyType.RANGE))
                .attributeDefinitions(
                        attr(Keys.PK), attr(Keys.SK),
                        attr(Keys.GSI1PK), attr(Keys.GSI1SK),
                        attr(Keys.GSI2PK), attr(Keys.GSI2SK),
                        attr(Keys.GSI3PK), attr(Keys.GSI3SK),
                        attr(Keys.GSI4PK), attr(Keys.GSI4SK))
                .globalSecondaryIndexes(
                        gsi(Keys.GSI1, Keys.GSI1PK, Keys.GSI1SK),
                        gsi(Keys.GSI2, Keys.GSI2PK, Keys.GSI2SK),
                        gsi(Keys.GSI3, Keys.GSI3PK, Keys.GSI3SK),
                        gsi(Keys.GSI4, Keys.GSI4PK, Keys.GSI4SK))
                .build());
        waitUntilActive(table);
        log.info("DynamoDB table {} is ACTIVE", table);
    }

    private void waitUntilActive(String table) {
        for (int i = 0; i < 40; i++) {
            try {
                var described = client.describeTable(DescribeTableRequest.builder().tableName(table).build());
                if (TableStatus.ACTIVE.equals(described.table().tableStatus())) {
                    boolean indexesReady = described.table().globalSecondaryIndexes() == null
                            || described.table().globalSecondaryIndexes().isEmpty()
                            || described.table().globalSecondaryIndexes().stream()
                            .allMatch(gsi -> "ACTIVE".equals(gsi.indexStatusAsString()));
                    if (indexesReady) {
                        return;
                    }
                }
            } catch (ResourceNotFoundException ignored) {
                // still creating
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted waiting for table " + table, e);
            }
        }
        throw new IllegalStateException("DynamoDB table did not become ACTIVE: " + table);
    }

    private static KeySchemaElement key(String name, KeyType type) {
        return KeySchemaElement.builder().attributeName(name).keyType(type).build();
    }

    private static AttributeDefinition attr(String name) {
        return AttributeDefinition.builder().attributeName(name).attributeType(ScalarAttributeType.S).build();
    }

    private static GlobalSecondaryIndex gsi(String name, String hash, String range) {
        return GlobalSecondaryIndex.builder()
                .indexName(name)
                .keySchema(key(hash, KeyType.HASH), key(range, KeyType.RANGE))
                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                .build();
    }
}
