package com.dw.athena.httpclient4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientTest {
	
	private static final String URI = "http://internalbristlecone.myaristata.com/_vti_bin/Lists.asmx";
	private static final String HEADER_SOAP_ACTIOIN = "http://schemas.microsoft.com/sharepoint/soap/GetList";
	private static final String HEADER_CONTENT_TYPE = "text/xml; charset=utf-8";

	public static void main(String[] args) {
		HttpClient httpClient = getNTLMEnableHttpClient();
		
		HttpPost request = new HttpPost(URI);
		request.setHeader("SOAPAction", HEADER_SOAP_ACTIOIN);
		request.setHeader(HttpHeaders.CONTENT_TYPE, HEADER_CONTENT_TYPE);
		
		StringEntity entity = new StringEntity(getRequestBody(), StandardCharsets.UTF_8);
		request.setEntity(entity);
		
		try {
			HttpResponse response = httpClient.execute(request);
			System.out.println(response.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static HttpClient getNTLMEnableHttpClient() {
		// Register NTLMSchemeFactory with the HttpClient instance you want  to NTLM enable
		final NTCredentials nt = new NTCredentials("AristataSvc", "bcone@investment31", "KERIKAWIN7", "Bristlecone");
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, nt);
		Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
		        .register(AuthSchemes.NTLM, new JCIFSNTLMSchemeFactory())
		        //.register(AuthSchemes.BASIC, new BasicSchemeFactory())
		        //.register(AuthSchemes.DIGEST, new DigestSchemeFactory())
		        //.register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
		        //.register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
		        .build();
		CloseableHttpClient httpClient = HttpClients.custom()
		        .setDefaultAuthSchemeRegistry(authSchemeRegistry)
		        .setDefaultCredentialsProvider(credentialsProvider)
		        .build();
		
		return httpClient;
	}
	
	private static String getRequestBody() {
		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
				  "<soap:Body>" +
				    "<GetList xmlns=\"http://schemas.microsoft.com/sharepoint/soap/\">" +
				      "<listName>Families</listName>" +
				    "</GetList>" +
				  "</soap:Body>" +
				"</soap:Envelope>";
	}

}
