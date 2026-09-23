package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.domain.Keys;
import ai.interviewhq.api.domain.SourceSeed;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SourceSeedRepository {

    private final DynamoOperations dynamo;

    public SourceSeedRepository(DynamoOperations dynamo) {
        this.dynamo = dynamo;
    }

    public Optional<SourceSeed> find(String sourceId, String seedId) {
        return dynamo.get(Keys.sourcePk(sourceId), Keys.seedSk(seedId)).map(ItemMapper::toSourceSeed);
    }

    public List<SourceSeed> findBySource(String sourceId) {
        return dynamo.queryByPk(Keys.sourcePk(sourceId), "SEED#", 200, null, true, ItemMapper::toSourceSeed)
                .getItems();
    }

    public PageResult<SourceSeed> findAll(int limit, String cursor) {
        return dynamo.queryGsi(Keys.GSI4, Keys.GSI4PK, Keys.gsi4pkEntityType(Keys.ENTITY_SOURCE_SEED),
                limit, cursor, false, ItemMapper::toSourceSeed);
    }

    public void save(SourceSeed seed) {
        dynamo.put(ItemMapper.fromSourceSeed(seed));
    }

    public boolean createIfAbsent(SourceSeed seed) {
        return dynamo.putIfAbsent(ItemMapper.fromSourceSeed(seed));
    }

    public void delete(String sourceId, String seedId) {
        dynamo.delete(Keys.sourcePk(sourceId), Keys.seedSk(seedId));
    }
}
