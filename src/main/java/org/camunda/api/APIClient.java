package org.camunda.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class APIClient {

    private final Logger logger = Logger.getLogger(APIClient.class.getName());

    private final String urlServer;

    /**
     * Get all information to connect the Camunda server
     *
     * @param urlServer url to connect the server
     */
    public APIClient(String urlServer) {
        this.urlServer = urlServer+"/engine-rest";
    }


    public String getBaseUrl() {
        return this.urlServer;
    }
    public ProcessInstance getProcessInstance() {
        return new ProcessInstance(this);
    }

    /**
     *
     * @param urlApi urlApi to call
     * @param parameters parameters to give to the Post URL
     * @return the payload
     * @throws Exception any error during the execution
     */
    protected Object doPost(String urlApi, Map<String, Object> parameters) throws Exception {

        URL url = new URL(urlServer + urlApi);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        JSONObject jo = new JSONObject(parameters);
        String jsonInputString = jo.toString();

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return toMap( new JSONObject(response.toString()));
        } catch (Exception e) {
            logger.severe("Can't decode answer "+e);
        }

        return null;

    }


    protected Object doGet(String urlApi) throws Exception {
        URL url = new URL(urlServer + urlApi);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return toMap( new JSONObject(response.toString()));

        } catch (Exception e) {
            logger.severe("Can't decode answer "+e);
        }

        return null;

    }


    private static Map<String, Object> toMap(JSONObject jsonobj)  throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }   return map;
    }
    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }   return list;
    }
}
