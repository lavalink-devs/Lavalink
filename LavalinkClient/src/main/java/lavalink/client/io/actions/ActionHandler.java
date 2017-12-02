package lavalink.client.io.actions;

import lavalink.client.io.Lavalink;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;

public class ActionHandler {

    private final Lavalink lavalink;
    private final OkHttpClient http;

    public ActionHandler(Lavalink lavalink) {
        this.lavalink = lavalink;

        http = new OkHttpClient.Builder()
                .build();
    }

    void execute(LinkAction action) {
        Call call = http.newCall(action.getRequest());
        //noinspection NullableProblems
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                action.onFailure.accept(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                action.handleResponse(response);
            }
        });
    }
}
