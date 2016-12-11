package app;

import app.executer.BoltCypherExecutor;
import app.executer.CypherExecutor;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class GraphModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GraphService.class).in(Scopes.SINGLETON);
        bind(CypherExecutor.class).to(BoltCypherExecutor.class);
    }
}
