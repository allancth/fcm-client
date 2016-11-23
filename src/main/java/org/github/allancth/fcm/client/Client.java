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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.sun.glass.events.KeyEvent;

public class Client {

    private Socket socket;

    private JTextField hostTextField;

    private JTextField portTextField;

    private JTextField userTextField;

    private JTextField keyTextField;

    private JTextArea logTextArea;

    private JTextArea messageTextArea;

    private JLabel statusLabel;

    private JButton connect;

    private JButton send;

    public static void main(final String[] args) {
        new Client().start();
    }

    public void start() {

        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // ignore
                }

                JFrame frame = new JFrame("FCM Client");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(true);
                frame.setMinimumSize(new Dimension(800, 600));
                frame.addWindowListener(new WindowListener() {

                    @Override
                    public void windowOpened(WindowEvent arg0) {
                    }

                    @Override
                    public void windowIconified(WindowEvent arg0) {
                    }

                    @Override
                    public void windowDeiconified(WindowEvent arg0) {
                    }

                    @Override
                    public void windowDeactivated(WindowEvent arg0) {
                    }

                    @Override
                    public void windowClosing(WindowEvent arg0) {
                        if (socket != null && socket.isConnected() && !socket.isClosed()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void windowClosed(WindowEvent arg0) {
                    }

                    @Override
                    public void windowActivated(WindowEvent arg0) {
                    }
                });

                FlowLayout flowLayout = new FlowLayout();
                flowLayout.setAlignment(FlowLayout.LEFT);
                Container contentPane = frame.getContentPane();
                contentPane.setLayout(new BorderLayout());

                // Connection Panel
                JPanel connectionPanel = new JPanel();
                connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.Y_AXIS));
                connectionPanel.setBorder(BorderFactory.createLoweredBevelBorder());
                contentPane.add(connectionPanel, BorderLayout.NORTH);

                JPanel hostAndPortPanel = new JPanel();
                connectionPanel.add(hostAndPortPanel);
                hostAndPortPanel.setLayout(flowLayout);
                hostAndPortPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                hostAndPortPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel hostLabel = new JLabel("HOST");
                hostTextField = new JTextField();
                hostTextField.setPreferredSize(new Dimension(192, 24));
                hostTextField.setText("fcm-xmpp.googleapis.com");
                hostAndPortPanel.add(hostLabel);
                hostAndPortPanel.add(hostTextField);

                JLabel portLabel = new JLabel("PORT");
                portTextField = new JTextField();
                portTextField.setPreferredSize(new Dimension(64, 24));
                portTextField.setText("5236");
                hostAndPortPanel.add(portLabel);
                hostAndPortPanel.add(portTextField);

                JPanel userAndKeyPanel = new JPanel();
                connectionPanel.add(userAndKeyPanel);
                userAndKeyPanel.setLayout(flowLayout);
                userAndKeyPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                userAndKeyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel userLabel = new JLabel("USER");
                userTextField = new JTextField();
                userTextField.setPreferredSize(new Dimension(192, 24));
                userAndKeyPanel.add(userLabel);
                userAndKeyPanel.add(userTextField);

                JLabel keyLabel = new JLabel("KEY");
                keyTextField = new JTextField();
                keyTextField.setPreferredSize(new Dimension(192, 24));
                userAndKeyPanel.add(keyLabel);
                userAndKeyPanel.add(keyTextField);

                // Center Panel
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.fill = GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 1;
                gridBagConstraints.weighty = 3;
                JPanel centerPanel = new JPanel();
                centerPanel.setLayout(new GridBagLayout());
                contentPane.add(centerPanel, BorderLayout.CENTER);

                JPanel logPanel = new JPanel();
                logPanel.setLayout(new BorderLayout());
                logPanel.setBorder(BorderFactory.createLoweredBevelBorder());
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                centerPanel.add(logPanel, gridBagConstraints);

                JLabel logLabel = new JLabel("Log");
                logLabel.setLayout(flowLayout);
                logLabel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                logLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                logPanel.add(logLabel, BorderLayout.NORTH);

                logTextArea = new JTextArea();
                logTextArea.setEditable(false);
                logTextArea.setBackground(new Color(211, 211, 211));
                logTextArea.setLineWrap(true);
                logTextArea.setMaximumSize(new Dimension(0, 100));
                logTextArea.setMinimumSize(new Dimension(0, 100));
                JScrollPane incomingTextAreaScroll = new JScrollPane(logTextArea, 
                                                                     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                                                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                logPanel.add(incomingTextAreaScroll, BorderLayout.CENTER);

                JPanel outgoingPanel = new JPanel();
                outgoingPanel.setLayout(new BorderLayout());
                outgoingPanel.setBorder(BorderFactory.createLoweredBevelBorder());
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                centerPanel.add(outgoingPanel, gridBagConstraints);

                JLabel outgoingLabel = new JLabel("Message");
                outgoingLabel.setLayout(flowLayout);
                outgoingLabel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                outgoingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                outgoingPanel.add(outgoingLabel, BorderLayout.NORTH);

                messageTextArea = new JTextArea();
                messageTextArea.setText("<message id=\"" + UUID.randomUUID().toString() + "\"><gcm xmlns=\"google:mobile:data\">{\n" + 
                                        "\"to\":\"device-registration-id\",\n" + 
                                        "\"notification\":{\"title\":\"Portugal vs. Denmark\",\"body\":\"5 to 1\"},\n" + 
                                        "\"message_id\":\"" + UUID.randomUUID().toString() + "\",\n" + 
                                        "\"time_to_live\":600\n" + 
                                        "}</gcm></message>");
                messageTextArea.setEditable(false);
                messageTextArea.setBackground(new Color(211, 211, 211));
                messageTextArea.setLineWrap(true);
                messageTextArea.setMaximumSize(new Dimension(0, 100));
                messageTextArea.setMinimumSize(new Dimension(0, 100));
                JScrollPane outgoingTextAreaScroll = new JScrollPane(messageTextArea, 
                                                                     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                                                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                outgoingPanel.add(outgoingTextAreaScroll, BorderLayout.CENTER);

                // Status Panel
                JPanel statusPanel = new JPanel();
                statusPanel.setLayout(flowLayout);
                statusPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPane.add(statusPanel, BorderLayout.SOUTH);

                statusLabel = new JLabel("Not connected");
                statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
                statusPanel.add(statusLabel);

                // Buttons
                connect = new JButton("CONNECT");
                connect.setMnemonic(KeyEvent.VK_C);
                userAndKeyPanel.add(connect);
                send = new JButton("SEND");
                send.setEnabled(false);
                send.setMnemonic(KeyEvent.VK_S);
                userAndKeyPanel.add(send);

                final ActionListener actionListener = new ActionListener() {

                    private FcmThread fcmThread;

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (ae.getSource() == connect) {
                            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                                logTextArea.setText(null);
                                hostTextField.setEnabled(false);
                                portTextField.setEnabled(false);
                                userTextField.setEnabled(false);
                                keyTextField.setEnabled(false);
                                statusLabel.setText("Connecting");
                                
                                try {
                                    final SSLContext sslContext = SSLContext.getInstance("TLS");
                                    sslContext.init(null, null, new java.security.SecureRandom());
                                    socket = sslContext.getSocketFactory().createSocket(hostTextField.getText(), 
                                                                                        Integer.parseInt(portTextField.getText()));

                                    final InputStream is = socket.getInputStream();
                                    final OutputStream os = socket.getOutputStream();
                                    AuthenticationThread authenticationThread = new AuthenticationThread(is, os, new AuthenticationCallback() {

                                        @Override
                                        public void onInbound(final String message) {
                                            logTextArea.insert(new Date() + " INBOUND\n" + message + "\n\n", 0);
                                        }

                                        @Override
                                        public void onOutbound(String message) {
                                            logTextArea.insert(new Date() + " OUTBOUND\n" + message + "\n\n", 0);
                                        }

                                        @Override
                                        public void onAuthenticationSuccess() {
                                            fcmThread = new FcmThread(is, os, new FcmCallback() {

                                                private void disconnect() {
                                                    hostTextField.setEnabled(true);
                                                    portTextField.setEnabled(true);
                                                    userTextField.setEnabled(true);
                                                    keyTextField.setEnabled(true);
                                                    connect.setEnabled(true);
                                                    connect.setText("CONNECT");
                                                    messageTextArea.setEditable(false);
                                                    messageTextArea.setBackground(new Color(211, 211, 211));
                                                    statusLabel.setText("Disconnected");
                                                    send.setEnabled(false);
                                                }

                                                @Override
                                                public void onMessageSent() {
                                                    send.setEnabled(true);
                                                }

                                                @Override
                                                public void onException(Exception e) {
                                                    disconnect();
                                                }

                                                @Override
                                                public void onDisconnect() {
                                                    disconnect();
                                                }

                                                @Override
                                                public void onInbound(String message) {
                                                    logTextArea.insert(new Date() + " INBOUND\n" + message + "\n\n", 0);
                                                }

                                                @Override
                                                public void onOutbound(String message) {
                                                    logTextArea.insert(new Date() + " OUTBOUND\n" + message + "\n\n", 0);
                                                }

                                            });
                                            new Thread(fcmThread).start();

                                            statusLabel.setText("Connected");
                                            messageTextArea.setEditable(true);
                                            messageTextArea.setBackground(Color.WHITE);
                                            send.setEnabled(true);
                                        }

                                        @Override
                                        public void onAuthenticationFailed() {
                                            statusLabel.setText("Authentication failed");
                                        }

                                    });
                                    authenticationThread.setUser(userTextField.getText());
                                    authenticationThread.setKey(keyTextField.getText());
                                    new Thread(authenticationThread).start();

                                    connect.setText("DISCONNECT");

                                } catch (Exception e) {
                                    connect.setText("CONNECT");
    
                                } finally {
                                    connect.setEnabled(true);
                                }
    
                            } else {
                                try {
                                    if (fcmThread != null) {
                                        fcmThread.stop();
                                        fcmThread = null;
                                    }

                                    if (socket != null) {
                                        socket.close();
                                        socket = null;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();

                                } finally {
                                    hostTextField.setEnabled(true);
                                    portTextField.setEnabled(true);
                                    userTextField.setEnabled(true);
                                    keyTextField.setEnabled(true);
                                    connect.setEnabled(true);
                                    connect.setText("CONNECT");
                                    messageTextArea.setEditable(false);
                                    messageTextArea.setBackground(new Color(211, 211, 211));
                                    statusLabel.setText("Disconnected");
                                }
                            }

                        } else if (ae.getSource() == send) {
                            if (fcmThread != null) {
                                send.setEnabled(false);
                                fcmThread.send(messageTextArea.getText());
                            }
                        }
                    }
                };

                connect.addActionListener(actionListener);
                send.addActionListener(actionListener);

                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    interface AuthenticationCallback {

        void onAuthenticationSuccess();

        void onAuthenticationFailed();

        void onInbound(String message);

        void onOutbound(String message);

    }

    interface FcmCallback {

        void onMessageSent();
        
        void onException(Exception e);

        void onDisconnect();

        void onInbound(String message);

        void onOutbound(String message);
    }
}
