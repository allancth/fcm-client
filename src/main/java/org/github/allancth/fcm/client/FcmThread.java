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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.github.allancth.fcm.client.Client.FcmCallback;

class FcmThread implements Runnable {

    private final InputStream is;

    private final OutputStream os;

    private final FcmCallback fcmCallback;

    private boolean isRunning;

    public FcmThread(final InputStream is, final OutputStream os, final FcmCallback fcmCallback) {
        this.is = is;
        this.os = os;
        this.fcmCallback = fcmCallback;
        isRunning = true;
    }

    @Override
    public void run() {
        final BufferedInputStream bis = new BufferedInputStream(is);
        while (isRunning) {
            try {
                final byte[] bytes = new byte[4096];
                bis.read(bytes);

                final String response = new String(bytes).intern().trim();
                if (!response.isEmpty()) {
                    fcmCallback.onInbound(response);
                }
            } catch (final SocketException e) {
                fcmCallback.onDisconnect();

            } catch (final Exception e) {
                e.printStackTrace();
                fcmCallback.onException(e);
            }
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void send(final String message) {
        try {
            os.write(message.getBytes());
            fcmCallback.onOutbound(message);
            fcmCallback.onMessageSent();

        } catch (final Exception e) {
            e.printStackTrace();
            fcmCallback.onException(e);
        }
    }
}
