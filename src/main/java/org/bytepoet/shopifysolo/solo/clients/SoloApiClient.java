package org.bytepoet.shopifysolo.solo.clients;

import java.util.Map;
import org.bytepoet.shopifysolo.solo.models.SoloReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



@Service
public class SoloApiClient {
	
	private static final Logger logger = LoggerFactory.getLogger(SoloApiClient.class);
	
	@Value("${solo-api-token}")
	private String apiToken;
	
	@Value("${soloapi.rooturl}")
	private String rootUrl;
	
	@Autowired
	private SoloMapper mapper;
	
	
	
	public String createReceipt(SoloReceipt receipt) {
		String endpoint = "/racun";
		
		MultiValueMap<String, String> parameters = mapper.map(receipt);
		parameters.add("token", apiToken);
		
		String url = buildUri(rootUrl+endpoint,parameters);
				
		
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(null, new byte[]{});
		Request request = new Request.Builder().url(url).post(body).build();
		try {
			logger.debug("Calling url: " + url);
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				throw new RuntimeException("statusCode: " + response.code() + " body: " + response.body().string());
			}
			ObjectMapper mapper = new ObjectMapper();
			String responseBody = response.body().string();
			SoloResponse soloResponse = mapper.readValue(responseBody, SoloResponse.class);
			if (soloResponse.status != 0) {
				throw new RuntimeException("statusCode: " + response.code() + " body: " + responseBody);
			}
			return soloResponse.receipt.get("pdf").toString();
			
		} catch (Exception e) {
			throw new RuntimeException( e.getMessage(), e);
		}

	}
	
	
	private String buildUri(String url, MultiValueMap<String, String> params) {
	    UriComponents uriComponents = UriComponentsBuilder.newInstance()
	            .queryParams(params).build();

	   return url+uriComponents.toString();
	}
	
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class SoloResponse {
		@JsonProperty
		private int status;
		@JsonProperty
		private String message;
		
		@JsonProperty("racun")
		private Map<String, Object> receipt;
	}

}
