/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.client.io;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

public abstract class ReusableWebSocket {

    private DisposableSocket socket;
    private final URI serverUri;
    private final Draft draft;
    private final Map<String, String> headers;
    private final int connectTimeout;
    private final ReusableWebSocket instance = this; // For use in inner class

    public ReusableWebSocket(URI serverUri, Draft draft, Map<String, String> headers, int connectTimeout) {
        this.serverUri = serverUri;
        this.draft = draft;
        this.headers = headers;
        this.connectTimeout = connectTimeout;
    }

    public abstract void onOpen(ServerHandshake handshakeData);
    public abstract void onMessage(String message);
    public abstract void onClose(int code, String reason, boolean remote);
    public abstract void onError(Exception ex);

    public void send(String text) {
        if (socket != null && socket.isOpen()) {
            socket.send(text);
        }
    }

    public InetSocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public boolean isOpen() {
        return socket != null && socket.isOpen();
    }

    public boolean isConnecting() {
        return socket != null && socket.isConnecting();
    }

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    public boolean isClosing() {
        return socket != null && socket.isClosing();
    }

    public void connect() {
        if (socket == null) socket = new DisposableSocket(serverUri, draft, headers, connectTimeout);
        socket.connect();
    }

    public void connectBlocking() throws InterruptedException {
        if (socket == null) socket = new DisposableSocket(serverUri, draft, headers, connectTimeout);
        socket.connectBlocking();
    }

    private class DisposableSocket extends WebSocketClient {

        DisposableSocket(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
            super(serverUri, protocolDraft, httpHeaders, connectTimeout);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            instance.onOpen(handshakedata);
        }

        @Override
        public void onMessage(String message) {
            instance.onMessage(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            instance.onClose(code, reason, remote);
        }

        @Override
        public void onError(Exception ex) {
            instance.onError(ex);
        }
    }

}
