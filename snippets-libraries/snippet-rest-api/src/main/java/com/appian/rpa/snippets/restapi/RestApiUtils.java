package com.appian.rpa.snippets.restapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

import net.sf.json.JSONObject;

/**
 * 
 * /** Class to manage the actions referred to a REST API utility
 *
 */

public class RestApiUtils {

	/**
	 * RestApiUtils instance
	 */
	private static RestApiUtils restApiUtilsInstance;

	/**
	 * Private constructor restricted to this class itself
	 * 
	 */
	private RestApiUtils() {

	}

	/**
	 * 
	 * Static method to create instance of RestApiUtils class
	 * 
	 * @return {@link RestApiUtils} instance
	 */
	public static RestApiUtils getInstance() {
		if (restApiUtilsInstance == null) {
			restApiUtilsInstance = new RestApiUtils();
		}

		return restApiUtilsInstance;
	}

	/**
	 * 
	 * Call an API Rest endpoint with the given parameters
	 * 
	 * @param consoleUrl URL of the console where the endpoint is hosted
	 * @param endpoint   Endpoint to call
	 * @param apiKey     Console API Key
	 * @param body       Request body
	 * 
	 * @throws IOException
	 */
	public void restApiCall(String consoleUrl, String endpoint, String apiKey, Map<String, Object> body)
			throws IOException {

		StringEntity input = getBodyMap(body);

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(consoleUrl + endpoint);
		post.setEntity(input);
		post.addHeader("Content-Type", "application/json");
		post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			if (line.contains("KO")) {
				throw new JidokaFatalException("Error calling the api rest endpoint" + ": " + line);
			}
		}
	}

	/**
	 * 
	 * Creates a StringEntity from a given map which contains the call body
	 * 
	 * @param body Map with the body parameters.
	 * 
	 * @return {@link StringEntity} with the body JSON on string format
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private StringEntity getBodyMap(Map<String, Object> body) throws UnsupportedEncodingException {

		JSONObject json = new JSONObject();

		for (Map.Entry<String, Object> entry : body.entrySet()) {
			json.put(entry.getKey(), entry.getValue());
		}

		return new StringEntity(json.toString());
	}

}
