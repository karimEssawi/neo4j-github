package app;

import app.executer.CypherExecutor;
import com.google.inject.Inject;
import org.neo4j.helpers.collection.Iterators;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import java.util.*;

import static org.neo4j.helpers.collection.MapUtil.map;

public class GraphService {
    private final CypherExecutor cypher;

    @Inject
    public GraphService(CypherExecutor cypher) {
        this.cypher = cypher;
    }

    Promise<Map> findRepository(String repoName) {
        if (repoName==null) return Promise.value(Collections.emptyMap());

        return Blocking.get(() -> {
                String statement = "MATCH (repo:REPO {repo_name:{1}}) \n" +
                        "OPTIONAL MATCH (repo)<-[r]-(dev:DEV) \n" +
                        "RETURN repo.repo_name as repo, collect({name:dev.dev_name}) as devs LIMIT 1";

            return Iterators.singleOrNull(cypher.query(statement, map("1", repoName)));
        });
    }

    @SuppressWarnings("unchecked")
    public Iterable<Map<String,Object>> search(String query) {
        if (query==null || query.trim().isEmpty()) return Collections.emptyList();
        return Iterators.asCollection(cypher.query(
                "MATCH (repo:REPO)\n" +
                        " WHERE repo.repo_name =~ {1}\n" +
                        " RETURN repo",
                map("1", "(?i).*"+query+".*")));
    }

    @SuppressWarnings("unchecked")
    Promise<Map<String, Object>> graph(int limit) {
        return Blocking.get(() -> {
            String statement = " MATCH (repo:REPO)<-[:CONTRIBUTES]-(dev:DEV) \n" +
                    " RETURN repo.repo_name as repo, collect(dev.dev_name) as devs \n" +
                    " LIMIT {1}";
            Iterator<Map<String,Object>> result = cypher.query(statement, map("1",limit));

//            Iterable<Map<String, Object>> iterable = () -> result;
//            Stream<Map<String, Object>> targetStream = StreamSupport.stream(iterable.spliterator(), false);

            List nodes = new ArrayList();
            List rels= new ArrayList();
            int i=0;
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                nodes.add(map("title",row.get("repo"),"label","repo"));
                int target=i;
                i++;
                for (Object name : (Collection) row.get("devs")) {
                    Map<String, Object> dev = map("title", name,"label","dev");
                    int source = nodes.indexOf(dev);
                    if (source == -1) {
                        nodes.add(dev);
                        source = i++;
                    }
                    rels.add(map("source",source,"target",target));
                }
            }
            return map("nodes", nodes, "links", rels);
        });
    }
}
