/* 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.github.allancth.fcm.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;

import org.github.allancth.fcm.client.Client.AuthenticationCallback;

class AuthenticationThread implements Runnable, CallbackHandler {

    private final InputStream is;

    private final OutputStream os;

    private final AuthenticationCallback authenticationCallback;

    private boolean isRunning;

    private boolean isAuthenticated;

    private String user;

    private String key;

    AuthenticationThread(final InputStream is, final OutputStream os, final AuthenticationCallback authenticationCallback) {
        this.is = is;
        this.os = os;
        this.authenticationCallback = authenticationCallback;
        isRunning = true;
    }

    @Override
    public void run() {
        final String stream = "<stream:stream to=\"gcm.googleapis.com\" version=\"1.0\" xmlns=\"jabber:client\" xmlns:stream=\"http://etherx.jabber.org/streams\">";
        try {
            os.write(stream.getBytes());
            authenticationCallback.onOutbound(stream);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final BufferedInputStream bis = new BufferedInputStream(is);
        while (isRunning) {
            try {
                final byte[] bytes = new byte[4096];
                bis.read(bytes);

                final String response = new String(bytes).intern().trim();
                if (!response.isEmpty()) {
                    authenticationCallback.onInbound(response);

                    if (response.startsWith("<stream:features><mechanisms xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">")) {
                        final SaslClient saslClient = Sasl.createSaslClient(new String[] { "PLAIN" }, null, "xmpp", user, new HashMap<String, String>(), this);
                        final String auth = "<auth mechanism=\"PLAIN\" xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">" + Base64.getEncoder().encodeToString(saslClient.evaluateChallenge(new byte[0])) + "</auth>";
                        if (saslClient.hasInitialResponse()) {
                            os.write(auth.getBytes());
                            authenticationCallback.onOutbound(auth);
                        }

                    } else if (response.startsWith("<failure xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">")) {
                        isRunning = false;
                        isAuthenticated = false;

                    } else if (response.startsWith("<success xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\"/>")) {
                        os.write(stream.getBytes());
                        authenticationCallback.onOutbound(stream);

                    } else if (response.startsWith("<stream:features><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"/><session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></stream:features>")) {
                        final String iq = "<iq type=\"set\"><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"></bind></iq>";
                        os.write(iq.getBytes());
                        authenticationCallback.onOutbound(iq);

                    } else if (response.startsWith("<iq type=\"result\"><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><jid>")) {
                        isAuthenticated = true;
                        isRunning = false;

                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
                isRunning = false;
                isAuthenticated = false;
            }
        }

        if (isAuthenticated) {
            authenticationCallback.onAuthenticationSuccess();

        } else {
            authenticationCallback.onAuthenticationFailed();
        }
    }

    void stop() {
        isRunning = false;
    }

    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (final Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                NameCallback nameCallback = (NameCallback) callback;
                nameCallback.setName(user);

            } else if (callback instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) callback;
                passwordCallback.setPassword(key.toCharArray());
            }
        }
    }

    void setUser(final String user) {
        this.user = user;
    }

    void setKey(final String key) {
        this.key = key;
    }
}
