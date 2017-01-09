package app;

import com.fasterxml.jackson.databind.ObjectMapper;
import ratpack.guice.Guice;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;

import java.util.Optional;

public class Main {
    public static void main(String... args) throws Exception {
        RatpackServer
                .start(server ->
                    server.serverConfig(c -> c.baseDir(BaseDir.find()))
                        .registry(Guice.registry(b -> b.module(GraphModule.class)))
                        .handlers(chain ->
                            chain.get("repo/:name", ctx -> {
                                ctx.get(GraphService.class)
                                        .findRepository(ctx.getPathTokens().get("name"))
                                        .onError(Throwable::printStackTrace)
                                        .map(r -> ctx.get(ObjectMapper.class).writeValueAsString(r))
                                        .then(json -> ctx.getResponse().send("application/json", json));
                            }).get("graph", ctx -> {
                                ctx.get(GraphService.class)
                                        .graph(Integer.parseInt(Optional.ofNullable(ctx.getRequest().getQueryParams().get("limit")).orElse("10")), ctx)
                                        .onError(Throwable::printStackTrace)
                                        .map(r -> ctx.get(ObjectMapper.class).writeValueAsString(r))
                                        .then(json -> ctx.getResponse().send("application/json", json));
                            }).files(files -> files.dir("public").indexFiles("index.html"))
                        )
                );
    }
}
