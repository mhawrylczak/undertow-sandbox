package mh.sandbox.undertow;


import sun.misc.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;

/**
Dla nie chunked undertow uruchmia handler gdy sa wszytkie dane

 client: Start Sending data = 10:32:48.173
 client: End Sending data = 10:32:52.172

 server: size request begin 10:32:52.175
 server: handler leave 10:32:52.176
 server: subscription closed 10:32:52.810


 dla chunked, jest inaczej - zaraz po połaczeniu jeszcze nim klient zacznie wysyłąc body juz jest odebrany request

 server: size request begin 10:50:18.922
 server: handler leave 10:50:18.923

 client: Start Sending data = 10:50:19.100
 client End Sending data = 10:50:32.138

 server: subscription closed 10:50:32.140


 */

public class UploadTest {

    public static void main(String[] args) throws IOException {
        int bufSize = 4096;
        File f = new File("/tmp/settings.xml");
        try(InputStream is = new BufferedInputStream(new FileInputStream(f))) {
            URL url = new URL("http://localhost:8080/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setChunkedStreamingMode(bufSize);
            try (OutputStream os = conn.getOutputStream()) {
                int nextByte;
                System.out.println("Start Sending data = " + LocalTime.now());
                while( -1  != (nextByte = is.read())){
                    os.write(nextByte);
                }
            }

            System.out.println("End Sending data = " + LocalTime.now());
            StringWriter output = new StringWriter();
            try(InputStream resp = conn.getInputStream()){
                int nextByte;
                while(-1 != (nextByte = resp.read())){
                    output.write(nextByte);
                }
            }
            conn.disconnect();
            System.out.println("output = " + output);

        }

    }
}
