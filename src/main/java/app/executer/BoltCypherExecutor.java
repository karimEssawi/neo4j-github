package app.executer;

import com.google.inject.Inject;
import org.neo4j.driver.v1.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BoltCypherExecutor implements CypherExecutor {
    private final org.neo4j.driver.v1.Driver driver;

    @Inject
    public BoltCypherExecutor() {
        driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "P@ssw0rd"));
//        driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "P@ssw0rd"), Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig());
    }

    @Override
    public Iterator<Map<String, Object>> query(String query, Map<String, Object> params) {
        try (Session session = driver.session()) {
            List<Map<String, Object>> list = session.run(query, params)
                    .list( r -> r.asMap(BoltCypherExecutor::convert));
            return list.iterator();
        }
    }

    private static Object convert(Value value) {
        switch (value.type().name()) {
            case "PATH":
                return value.asList(BoltCypherExecutor::convert);
            case "NODE":
            case "RELATIONSHIP":
                return value.asMap();
        }

        return value.asObject();
    }

}
