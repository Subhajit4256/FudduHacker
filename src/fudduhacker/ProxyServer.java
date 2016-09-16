/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fudduhacker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author BIT2
 */
public class ProxyServer {

    public static void main(String[] args) throws IOException {
        new ProxyServer().runServer();
    }

    public void runServer() throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        int port = 9999;	//default

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Started on: " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        while (listening) {
            new ProxyThread(serverSocket.accept()).start();
        }
        serverSocket.close();
    }

    public class ProxyThread extends Thread {

        private Socket socket = null;
        private static final int BUFFER_SIZE = 32768;

        public ProxyThread(Socket socket) {
            super("ProxyThread");
            this.socket = socket;
        }

        public synchronized void run() {
            System.err.println("Thread ID===" + Thread.currentThread().getId());
            //get input from user
            //send request to server
            //get response from server
            //send response to user

            try {
                DataOutputStream dataoutclient
                        = new DataOutputStream(socket.getOutputStream());
                BufferedReader bufferedinclient = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                boolean isPost = false;
                String inputLine, outputLine;
                int cnt = 0;
                String urlToCall = "";
                String actualURL = "";
                StringBuilder sb = new StringBuilder();
                StringBuilder request = new StringBuilder();
                int length = 0, chars = 0, v = 0;
                char buffer[] = new char[1024];
                ///////////////////////////////////
                //begin get request from client                                
                /*while ((chars = bufferedinclient.read(buffer, 0, chars)) != -1) {                     
                 if (request.indexOf("GET") != -1 && request.indexOf("HTTP") != -1) {
                 //System.err.println("URL = " + urlToCall);
                 urlToCall = request.substring(request.lastIndexOf("GET") + 3, request.indexOf("HTTP"));
                 } else if (request.indexOf("POST") != -1 && request.indexOf("HTTP") != -1) {
                 //System.err.println("URL = "+urlToCall);
                 urlToCall = request.substring(request.lastIndexOf("POST") + 3, request.indexOf("HTTP"));
                 }                    
                 request.append(buffer);                    
                 }
                 System.err.println(request);

                 */
                //StringTokenizer token = new StringTokenizer(request.toString());
                while ((inputLine = bufferedinclient.readLine()) != null && inputLine.length() != 0) {
                    request.append(inputLine);
                    try {
                        StringTokenizer tok = new StringTokenizer(inputLine);
                        tok.nextToken();
                    } catch (Exception e) {
                        break;
                    }

                    if (inputLine.startsWith("Content-Length: ")) { // get the
                        // content-length
                        int index = inputLine.indexOf(':') + 1;
                        String len = inputLine.substring(index).trim();
                        length = Integer.parseInt(len);
                    }

                    //parse the first line of the request to find the url
                    if (cnt == 0) {
                        String[] tokens = inputLine.split(" ");
                        if (tokens[0].equals("POST")) {
                            isPost = true;
                        }
                        urlToCall = tokens[1];
                        System.out.println("URL=" + urlToCall);
                        if (urlToCall.indexOf("://") == -1) {
                            String l[] = urlToCall.split(":");
                            actualURL = l[0];
                            sb.append("https://").append(actualURL);
                            //urlToCall.concat("https://").concat(actualURL);
                            urlToCall = sb.toString();
                        }

                        //can redirect this to output log
                        System.out.println("Request for : " + urlToCall);
                    }

                    System.out.println("Tokens-" + inputLine);
                    cnt++;
                }
                StringBuilder acHeadres = new StringBuilder();
                String headers[] = request.toString().split("\n");
                for (String s : headers) {
                    acHeadres.append(s + "\r\n");
                    System.err.println(s + "\r\n");
                }
                //end get request from client
                ///////////////////////////////////
                //BufferedReader rd = null;
                try {
                    //System.out.println("sending request
                    //to real server for url: "
                    //        + urlToCall);
                    ///////////////////////////////////

                    StringBuilder body = new StringBuilder("");
                    char ara[] = new char[1000];
                    if (length > 0 && isPost == true) {
                        System.out.println("if satisfied");
                        int read;
                        while ((read = bufferedinclient.read()) != -1) {
                            body.append((char) read);
                            if (body.length() == length) {
                                break;
                            }
                        }
                    }
                    body.append(ara);
                    System.out.println("Body==== " + body);

                    //begin send request to server, get response from server
                    //URLConnection conn = url.openConnection();
                    //not doing HTTP posts
                    //conn.setDoOutput(true);
                    // Get the response
                    //huc.setDoInput(true);
                    //huc.setDoOutput(true);
                    //if (conn.getContentLength() > 0) {                   
                    String data = "";
                    int paramCount = 1;

                    if (isPost) {
                        URL url = new URL(urlToCall);
                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        System.err.println("POST");
                        huc.setDoOutput(true);
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(huc.getOutputStream(), "UTF8"));
                        String key_val[] = body.toString().split("&");
                        for (String k_v : key_val) {
                            System.err.println("params : " + k_v);
                            String params[] = k_v.split("=");
                            if (paramCount == key_val.length) {
                                if (params.length == 1) {
                                    System.err.println("key : " + params[0] + " ");
                                    data += URLEncoder.encode(params[0], "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
                                } else {
                                    System.err.println("key : " + params[0] + " value " + params[1]);
                                    data += URLEncoder.encode(params[0], "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                                }
                            } else {
                                if (params.length == 1) {
                                    System.err.println("key : " + params[0] + " ");
                                    data += URLEncoder.encode(params[0], "UTF-8") + "=" + URLEncoder.encode("", "UTF-8") + "&" + "";
                                } else {
                                    System.err.println("key : " + params[0] + " value " + params[1]);
                                    data += URLEncoder.encode(params[0], "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8") + "&" + "";
                                }
                            }
                            paramCount++;
                        }
                        //bufferedWriter.write(request.toString());
                        //bufferedWriter.write("POST " + urlToCall + " HTTP/1.1\r\n");
                        //Proxy-Connection: keep-alive
                        //bufferedWriter.write("Proxy-Connection: keep-alive");
                        //bufferedWriter.write("Host:" + "192.168.0.1" + "\r\n");

                        //bufferedWriter.write("User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8 ( .NET CLR 3.5.30729)\r\n");
                        //bufferedWriter.write("Accept: */*\r\n");
                        //bufferedWriter.write("Connection: Close\r\n");
                        //bufferedWriter.write("Content-Type: application/x-www-form-urlencoded\r\n");
                        //bufferedWriter.write("Content-Length: " + data.length() + "\r\n");
                        System.err.println("Before Writting...........");
                        //bufferedWriter.write(acHeadres.toString());
                        System.err.println("After Writting headers...........");
                        bufferedWriter.write(data);
                        System.err.println("After Writting data...........");
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        //BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        int readCount = 0;
                        byte by[] = new byte[BUFFER_SIZE];
                        InputStream iss = huc.getInputStream();
                        readCount = iss.read(by, 0, BUFFER_SIZE);
                        System.out.println("debug1");                        
                        while (readCount > 0) {
                            dataoutclient.write(by, 0, BUFFER_SIZE);
                            readCount = iss.read(by, 0, BUFFER_SIZE);
                        }                        
                    } else {
                        URL url = new URL(urlToCall);
                        InputStream inputstreamProxy = null;
                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        try {
                            inputstreamProxy = huc.getInputStream();
                            // rd = new BufferedReader(new InputStreamReader(inputstreamProxy));
                        } catch (IOException ioe) {
                            System.out.println(
                                    "********* IO EXCEPTION **********: " + ioe);
                        }
                        System.err.println("GET");
                        byte by[] = new byte[BUFFER_SIZE];
                        int index = inputstreamProxy.read(by, 0, BUFFER_SIZE);
                        while (index > 0) {
                            if (!socket.isClosed()) {
                                dataoutclient.write(by, 0, index);
                                index = inputstreamProxy.read(by, 0, BUFFER_SIZE);
                            }
                        }
                        dataoutclient.flush();
                    }
                    //}
                    //end send request to server, get response from server
                    ///////////////////////////////////

                    ///////////////////////////////////
                    //begin send response to client
                    //end send response to client
                    ///////////////////////////////////
                } catch (Exception e) {

                    e.printStackTrace();
                    //can redirect this to error log
                    System.err.println("Thread ID===" + Thread.currentThread().getId());
                    System.err.println("Encountered exception: " + e);
                    //encountered error - just send nothing back, so
                    //processing can continue
                    dataoutclient.writeBytes("");
                }

                //close out all resources               
                if (dataoutclient != null) {
                    dataoutclient.close();
                }
                if (bufferedinclient != null) {
                    bufferedinclient.close();
                }
                if (socket != null) {
                    socket.close();
                }

                System.err.println("Leaving..." + Thread.currentThread().getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
