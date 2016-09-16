/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fudduhacker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author BIT2
 */
public class FudduHacker {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
         ServerSocket server;
         try {
         server = new ServerSocket(8080);
        
         System.out.println("Listening for connection on port 8080 ....");
         while (true) {
         Socket clientSocket = server.accept();
         InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
         BufferedReader reader = new BufferedReader(isr);
         String line = reader.readLine();
         while (!line.isEmpty()) {
         System.out.println(line);
         line = reader.readLine();

         }
         }
         } catch (Exception ex) {
         //Logger.getLogger(FudduHacker.class.getName()).log(Level.SEVERE, null, ex);            
         }*/

        try {
            String host = "localhost";
            int remoteport = 80;
            int localport = 8080;
            // Print a start-up message
            System.out.println("Starting proxy for " + host + ":" + remoteport
                    + " on port " + localport);
            // And start running the server
            runServer(host, remoteport, localport); // never returns
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * runs a single-threaded proxy server on the specified local port. It never
     * returns.
     */
    public static void runServer(String host, int remoteport, int localport)
            throws IOException {
        // Create a ServerSocket to listen for connections with
        ServerSocket ss = new ServerSocket(localport);

        final byte[] request = new byte[1024];
        byte[] reply = new byte[4096];

        while (true) {
            Socket client = null, server = null;
            try {
                // Wait for a connection on the local port
                client = ss.accept();

                final InputStream streamFromClient = client.getInputStream();
                final OutputStream streamToClient = client.getOutputStream();

        // Make a connection to the real server.
                // If we cannot connect to the server, send an error to the
                // client, disconnect, and continue waiting for connections.
                try {
                    server = new Socket(host, remoteport);
                } catch (IOException e) {
                    PrintWriter out = new PrintWriter(streamToClient);
                    out.print("Proxy server cannot connect to " + host + ":"
                            + remoteport + ":\n" + e + "\n");
                    out.flush();
                    client.close();
                    continue;
                }

                // Get server streams.
                final InputStream streamFromServer = server.getInputStream();
                final OutputStream streamToServer = server.getOutputStream();
                final StringBuffer formattedReq=new StringBuffer();
        // a thread to read the client's requests and pass them
                // to the server. A separate thread for asynchronous.
                Thread t = new Thread() {
                    public void run() {
                        /*try {
                            InputStreamReader isr = new InputStreamReader(streamFromClient);
                            BufferedReader reader = new BufferedReader(isr);
                            String line = reader.readLine();
                            while (!line.isEmpty()) {
                                System.out.println(line);
                                formattedReq.append(line);
                                line = reader.readLine();
                            }                          
                            */
                                
                                
                                
                                
                                
                                
                                
                                
                                
                                int bytesRead;
                                try {
                                    while ((bytesRead = streamFromClient.read(request)) != -1) {
                                        System.out.println("Reading.........from client");
                                        streamToServer.write(request, 0, bytesRead);
                                        streamToServer.flush();
                                    }
                                } catch (IOException e) {
                                }
                                
                                // the client closed the connection to us, so close our
                                // connection to the server.
                                try {
                                    streamToServer.close();
                                } catch (IOException e) {
                                }
                               
                    }
                };

                // Start the client-to-server request thread running
                t.start();

        // Read the server's responses
                // and pass them back to the client.
                int bytesRead;
                try {
                    while ((bytesRead = streamFromServer.read(reply)) != -1) {
                        System.out.println("Writting.........to server");
                        streamToClient.write(reply, 0, bytesRead);
                        streamToClient.flush();
                    }
                } catch (IOException e) {
                }

        // The server closed its connection to us, so we close our
                // connection to our client.
                streamToClient.close();
            } catch (IOException e) {
                System.err.println(e);
            } finally {
                try {
                    if (server != null) {
                        server.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                } catch (IOException e) {
                }
            }
        }

    }
}
