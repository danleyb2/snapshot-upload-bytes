import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;


public class Main {
    static final String charset = "UTF-8";

    // Get api key from https://app.platerecognizer.com/start/ and replace MY_API_KEY
    static final String PLATERECOGNIZER_API_TOKEN = "77c2fabd94b7d0c9b6ac63c###################";

    public static void main(String[] args) {
       // In memory File
        try {
            File fi = new File("demo.jpg");
            byte[] fileContent = Files.readAllBytes(fi.toPath());
            String fileName = "snapshot.jpg";
            sendMemoryFile(fileName, fileContent);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void sendMemoryFile(String fileName, byte[] imageBytes) throws Exception {

        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
        InputStream imageInputStream = new ByteArrayInputStream(imageBytes);

        try {

            URL obj = new URL("https://api.platerecognizer.com/v1/plate-reader/");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Token " + PLATERECOGNIZER_API_TOKEN);
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("Accept", "application/json");

            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            // os.write(POST_PARAMS.getBytes());
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, charset), true);

            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"upload\"; filename=\"" + fileName + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = imageInputStream.read(buffer))!= -1){
                os.write(buffer,0, len);
            }

            os.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();
            os.close();

            int responseCode = con.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // print result
                System.out.println(response.toString());
            } else {
                System.out.println("POST failed");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}

