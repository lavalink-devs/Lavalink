package lavalink.client.io.actions;

import lavalink.client.io.Link;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public abstract class LinkAction {

    private static final Logger log = LoggerFactory.getLogger(LinkAction.class);
    private final Link link;
    private final String route;
    private final String body;

    @SuppressWarnings("WeakerAccess")
    public static Consumer DEFAULT_SUCCESS = o -> {};
    @SuppressWarnings("WeakerAccess")
    public static Consumer<Throwable> DEFAULT_FAILURE = t ->
            log.error("LinkAction queue returned failure: [{}] {}", t.getClass().getSimpleName(), t.getMessage());

    Consumer onSuccess = null;
    Consumer<Throwable> onFailure = null;

    public LinkAction(Link link, String route, String body) {
        this.link = link;
        this.route = route;
        this.body = body;
    }

    @SuppressWarnings("unused")
    public void queue() {
        queue(DEFAULT_SUCCESS, DEFAULT_FAILURE);
    }

    @SuppressWarnings("unused")
    public void queue(Consumer onSuccess) {
        queue(onSuccess, DEFAULT_FAILURE);
    }

    public void queue(Consumer onSuccess, Consumer<Throwable> onFailure) {
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        link.getLavalink().getActionHandler().execute(this);
    }

    Request getRequest() {
        Request.Builder builder = new Request.Builder();
        builder.method("POST", RequestBody.create(MediaType.parse("application/json"), body));
        // TODO
        return builder.build();
    }

    // TODO: complete method

    abstract void handleResponse(Response response);

}
