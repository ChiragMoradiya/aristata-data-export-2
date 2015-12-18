package com.dw.athena.httpclient4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
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

  private static final String HEADER_CONTENT_TYPE = "text/xml; charset=utf-8";
  private static HttpClient httpClient;

  public static void main(String[] args) {
    httpClient = getNTLMEnableHttpClient();
    writeListAndView(null, "UserInfo", "C:\\aristata-export\\master\\UserInfo.xml");
  }

  private static HttpClient getNTLMEnableHttpClient() {
    // Register NTLMSchemeFactory with the HttpClient instance you want to NTLM enable
    final NTCredentials nt =
        new NTCredentials("AristataSvc", "bcone@investment31", "KERIKAWIN7", "Bristlecone");
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, nt);
    Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
        .register(AuthSchemes.NTLM, new JCIFSNTLMSchemeFactory())
        // .register(AuthSchemes.BASIC, new BasicSchemeFactory())
        // .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
        // .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
        // .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
        .build();
    CloseableHttpClient httpClient =
        HttpClients.custom().setDefaultAuthSchemeRegistry(authSchemeRegistry)
            .setDefaultCredentialsProvider(credentialsProvider).build();

    return httpClient;
  }

  // private static void writeListCollection(HttpClient httpClient, String location, String family)
  // {
  // HttpPost request = new HttpPost(getURI(family));
  // request.setHeader("SOAPAction", getRequestHeader("GetListCollection"));
  // request.setHeader(HttpHeaders.CONTENT_TYPE, HEADER_CONTENT_TYPE);
  //
  // StringEntity entity = new StringEntity(getListCollectionRequestBody(), StandardCharsets.UTF_8);
  // request.setEntity(entity);
  // executeRequest(request, location);
  // }

  private static void writeListAndView(String family, String list, String location) {
    executeRequest(family, "GetListAndView", "<listName>" + list + "</listName>", location);
  }

  private static void executeRequest(String family, String method, String body, String location) {
    try {
      HttpPost request = new HttpPost(getURI(family));
      request.setHeader("SOAPAction", getRequestHeader(method));
      request.setHeader(HttpHeaders.CONTENT_TYPE, HEADER_CONTENT_TYPE);

      StringEntity entity = new StringEntity(getRequestBody(method, body), StandardCharsets.UTF_8);
      request.setEntity(entity);

      HttpResponse response = httpClient.execute(request);

      if (response.getStatusLine().getStatusCode() == 200) {
        writeToFile(response.getEntity(), location);
        System.out.println("executeRequest:: Response exported to file: " + location);
      } else {
        System.out.println("executeRequest::" + response.getStatusLine().toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void writeToFile(HttpEntity httpEntity, String fileLocation) {
    InputStream is = null;
    OutputStream os = null;
    try {
      is = httpEntity.getContent();

      File file = new File(fileLocation);
      file.getParentFile().mkdirs();
      os = new FileOutputStream(file);

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = is.read(buffer)) != -1) {
        os.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        is.close();
        os.close();
      } catch (IOException e) {
        // ignore
      }
    }
  }

  private static String getURI(String family) {
    if (family == null) {
      return "http://internalbristlecone.myaristata.com/_vti_bin/Lists.asmx";
    }

    return "http://internalbristlecone.myaristata.com/" + family + "/_vti_bin/Lists.asmx";
  }

  // private static String getListItemsRequestBody() {
  // return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
  // + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
  // xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"
  // xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
  // + "<soap:Body>" + " <GetListItems xmlns=\"http://schemas.microsoft.com/sharepoint/soap/\">"
  // + " <listName>" + LIST_NAME + "</listName>" + "<rowLimit>10000</rowLimit>"
  // + "</GetListItems>" + "</soap:Body>" + "</soap:Envelope>";
  // }

  private static String getRequestBody(String method, String body) {
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<soap:Body>" + " <" + method
        + " xmlns=\"http://schemas.microsoft.com/sharepoint/soap/\">" + body + "</" + method + ">"
        + "</soap:Body>" + "</soap:Envelope>";
  }

  private static String getRequestHeader(String method) {
    return "http://schemas.microsoft.com/sharepoint/soap/" + method;
  }

  // private static String getEntitiesDataRequestBody() {
  // String requestBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
  // + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
  // xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"
  // xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
  // + "<soap:Body>" + " <GetListItems xmlns=\"http://schemas.microsoft.com/sharepoint/soap/\">"
  // + "<listName>e6f2230a-edb4-4677-b730-e6cf7281fd2e</listName>"
  // + "<viewName>0349a7be-90ae-4c0f-b081-bb5289a4fe8b</viewName>" + "<query>"
  // + " <Query xmlns=\"\"/>" + "</query>" + "<viewFields>" + "<ViewFields>" + "<FieldRef "
  // + "Name=\"ID\"/>" + "<FieldRef " + "Name=\"_ModerationStatus\"/>" + "<FieldRef "
  // + "Name=\"_ModerationComments\"/>" + "<FieldRef " + " Name=\"AristataEntityFolder\"/>"
  // + "<FieldRef " + " Name=\"AristataAssocMgr1\"/>" + "<FieldRef "
  // + "Name=\"AristataAssocMgr2\"/>" + "<FieldRef " + " Name=\"AristataAssocMgr3\"/>"
  // + "<FieldRef " + "Name=\"AristataAssocMgr4\"/>" + "<FieldRef "
  // + "Name=\"AristataAssocSLEntities\"/>" + "<FieldRef " + "Name=\"Attachments\"/>"
  // + "<FieldRef " + "Name=\"AristataSLCompany\"/>" + "<FieldRef "
  // + "Name=\"AristataSLContact\"/>" + "<FieldRef " + "Name=\"ContentType\"/>" + "<FieldRef "
  // + "Name=\"ContentTypeId\"/>" + "<FieldRef " + "Name=\"_CopySource\"/>" + "<FieldRef "
  // + "Name=\"Created\"/>" + "<FieldRef " + "Name=\"Author\"/>" + "<FieldRef "
  // + "Name=\"EncodedAbsUrl\"/>" + "<FieldRef " + "Name=\"AristataSLFamily\"/>" + "<FieldRef "
  // + "Name=\"File_x0020_Type\"/>" + "<FieldRef " + "Name=\"GUID\"/>" + "<FieldRef "
  // + "Name=\"_HasCopyDestinations\"/>" + "<FieldRef " + "Name=\"InstanceID\"/>" + "<FieldRef "
  // + "Name=\"_IsCurrentVersion\"/>" + "<FieldRef " + "Name=\"_Level\"/>" + "<FieldRef "
  // + "Name=\"Modified\"/>" + "<FieldRef " + "Name=\"Editor\"/>" + "<FieldRef "
  // + "Name=\"FileLeafRef\"/>" + "<FieldRef " + "Name=\"Order\"/>" + "<FieldRef "
  // + "Name=\"owshiddenversion\"/>" + "<FieldRef " + "Name=\"FileDirRef\"/>" + "<FieldRef "
  // + "Name=\"AristataRelationshipMgr\"/>" + "<FieldRef " + "Name=\"Title\"/>" + "<FieldRef "
  // + "Name=\"AristataCLEntityType\"/>" + "<FieldRef " + "Name=\"_UIVersion\"/>" + "<FieldRef "
  // + "Name=\"FileRef\"/>" + "<FieldRef " + "Name=\"_UIVersionString\"/>" + "<FieldRef "
  // + "Name=\"WorkflowInstanceID\"/>" + "<FieldRef " + "Name=\"WorkflowVersion\"/>"
  // + "</ViewFields>" + "</viewFields>" + "<rowLimit>5000</rowLimit>" + "<queryOptions>"
  // + "<QueryOptions xmlns=\"\">" + "<DateInUtc>TRUE</DateInUtc>" + "</QueryOptions>"
  // + "</queryOptions>" + "<webID>3fbe3ea8-1a93-43c5-810d-e72bba19aa8f</webID>"
  // + "</GetListItems>" + "</soap:Body>" + "</soap:Envelope>";
  //
  // return requestBody;
  //
  // }

}
