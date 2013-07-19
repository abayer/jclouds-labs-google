/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.googlecomputeengine.features;

import com.google.common.collect.ImmutableMap;
import org.jclouds.googlecomputeengine.domain.Instance;
import org.jclouds.googlecomputeengine.domain.InstanceTemplate;
import org.jclouds.googlecomputeengine.internal.BaseGoogleComputeEngineApiExpectTest;
import org.jclouds.googlecomputeengine.parse.ParseInstanceListTest;
import org.jclouds.googlecomputeengine.parse.ParseInstanceSerialOutputTest;
import org.jclouds.googlecomputeengine.parse.ParseInstanceTest;
import org.jclouds.googlecomputeengine.parse.ParseOperationTest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.net.URI;

import static java.net.URI.create;
import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.COMPUTE_READONLY_SCOPE;
import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.COMPUTE_SCOPE;
import static org.jclouds.googlecomputeengine.features.ProjectApiExpectTest.GET_PROJECT_REQUEST;
import static org.jclouds.googlecomputeengine.features.ProjectApiExpectTest.GET_PROJECT_RESPONSE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

/**
 * @author David Alves
 */
@Test(groups = "unit")
public class InstanceApiExpectTest extends BaseGoogleComputeEngineApiExpectTest {

   public static final HttpRequest GET_INSTANCE_REQUEST = HttpRequest
           .builder()
           .method("GET")
           .endpoint("https://www.googleapis" +
                   ".com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances/test-1")
           .addHeader("Accept", "application/json")
           .addHeader("Authorization", "Bearer " + TOKEN).build();


   public static final HttpResponse GET_INSTANCE_RESPONSE = HttpResponse.builder().statusCode(200)
           .payload(staticPayloadFromResource("/instance_get.json")).build();

   public static final HttpRequest LIST_INSTANCES_REQUEST = HttpRequest
           .builder()
           .method("GET")
           .endpoint("https://www.googleapis" +
                   ".com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances")
           .addHeader("Accept", "application/json")
           .addHeader("Authorization", "Bearer " + TOKEN).build();

   public static final HttpResponse LIST_INSTANCES_RESPONSE = HttpResponse.builder().statusCode(200)
           .payload(staticPayloadFromResource("/instance_list.json")).build();

   public static final HttpRequest LIST_CENTRAL1B_INSTANCES_REQUEST = HttpRequest
           .builder()
           .method("GET")
           .endpoint("https://www.googleapis" +
                   ".com/compute/v1beta15/projects/myproject/zones/us-central1-b/instances")
           .addHeader("Accept", "application/json")
           .addHeader("Authorization", "Bearer " + TOKEN).build();

   public static final HttpResponse LIST_CENTRAL1B_INSTANCES_RESPONSE = HttpResponse.builder().statusCode(200)
           .payload(staticPayloadFromResource("/instance_list_central1b_empty.json")).build();

   public static final HttpResponse CREATE_INSTANCE_RESPONSE = HttpResponse.builder().statusCode(200)
           .payload(staticPayloadFromResource("/zone_operation.json")).build();


   public void testGetInstanceResponseIs2xx() throws Exception {

      InstanceApi api = requestsSendResponses(
              requestForScopes(COMPUTE_READONLY_SCOPE), TOKEN_RESPONSE,
              GET_INSTANCE_REQUEST, GET_INSTANCE_RESPONSE).getInstanceApiForProject("myproject");

      assertEquals(api.getInZone("us-central1-a", "test-1"), new ParseInstanceTest().expected());
   }

   public void testGetInstanceResponseIs4xx() throws Exception {

      HttpResponse operationResponse = HttpResponse.builder().statusCode(404).build();

      InstanceApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, GET_INSTANCE_REQUEST, operationResponse).getInstanceApiForProject("myproject");

      assertNull(api.getInZone("us-central1-a", "test-1"));
   }

   public void testGetInstanceSerialPortOutput() throws Exception {
      HttpRequest get = HttpRequest
              .builder()
              .method("GET")
              .endpoint("https://www.googleapis" +
                      ".com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances/test-1/serialPort")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(200)
              .payload(payloadFromResource("/instance_serial_port.json")).build();


      InstanceApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, get, operationResponse).getInstanceApiForProject("myproject");

      assertEquals(api.getSerialPortOutputInZone("us-central1-a", "test-1"), new ParseInstanceSerialOutputTest().expected());
   }

   public void testInsertInstanceResponseIs2xxNoOptions() {
      HttpRequest insert = HttpRequest
              .builder()
              .method("POST")
              .endpoint("https://www.googleapis.com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN)
              .payload(payloadFromResourceWithContentType("/instance_insert_simple.json", MediaType.APPLICATION_JSON))
              .build();

      InstanceApi api = requestsSendResponses(ImmutableMap.of(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, GET_PROJECT_REQUEST, GET_PROJECT_RESPONSE,
              requestForScopes(COMPUTE_SCOPE),
              TOKEN_RESPONSE, insert,
              CREATE_INSTANCE_RESPONSE)).getInstanceApiForProject("myproject");

      InstanceTemplate options = InstanceTemplate.builder().forMachineType("us-central1-a/n1-standard-1")
              .image(URI.create("https://www.googleapis.com/compute/v1beta15/projects/google/global/images/gcel-12-04-v20121106"))
              .addNetworkInterface(URI.create("https://www.googleapis" +
                      ".com/compute/v1beta15/projects/myproject/global/networks/default"));

      assertEquals(api.createInZone("us-central1-a", "test-1", options), new ParseOperationTest().expected());
   }

   public void testInsertInstanceResponseIs2xxAllOptions() {
      HttpRequest insert = HttpRequest
              .builder()
              .method("POST")
              .endpoint("https://www.googleapis.com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN)
              .payload(payloadFromResourceWithContentType("/instance_insert.json", MediaType.APPLICATION_JSON))
              .build();

      HttpResponse insertInstanceResponse = HttpResponse.builder().statusCode(200)
              .payload(payloadFromResource("/zone_operation.json")).build();

      InstanceApi api = requestsSendResponses(ImmutableMap.of(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, GET_PROJECT_REQUEST, GET_PROJECT_RESPONSE,
              requestForScopes(COMPUTE_SCOPE),
              TOKEN_RESPONSE, insert, insertInstanceResponse)).getInstanceApiForProject("myproject");

      InstanceTemplate options = InstanceTemplate.builder().forMachineType("us-central1-a/n1-standard-1")
              .addNetworkInterface(URI.create("https://www.googleapis" +
                      ".com/compute/v1beta15/projects/myproject/global/networks/default"), Instance.NetworkInterface.AccessConfig.Type.ONE_TO_ONE_NAT)
              .description("desc")
              .image(URI.create("https://www.googleapis" +
                      ".com/compute/v1beta15/projects/google/global/images/gcel-12-04-v20121106"))
              .addDisk(InstanceTemplate.PersistentDisk.Mode.READ_WRITE,
                      create("https://www.googleapis.com/compute/v1beta15/projects/myproject/zones/us-central1-a/disks/test"))
              .addTag("aTag")
              .addServiceAccount(Instance.ServiceAccount.builder().email("default").addScopes("myscope").build())
              .addMetadata("aKey", "aValue");

      assertEquals(api.createInZone("us-central1-a", "test-0", options),
              new ParseOperationTest().expected());
   }

   public void testDeleteInstanceResponseIs2xx() {
      HttpRequest delete = HttpRequest
              .builder()
              .method("DELETE")
              .endpoint("https://www.googleapis" +
                      ".com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances/test-1")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse deleteResponse = HttpResponse.builder().statusCode(200)
              .payload(payloadFromResource("/zone_operation.json")).build();

      InstanceApi api = requestsSendResponses(requestForScopes(COMPUTE_SCOPE),
              TOKEN_RESPONSE, delete, deleteResponse).getInstanceApiForProject("myproject");

      assertEquals(api.deleteInZone("us-central1-a", "test-1"),
              new ParseOperationTest().expected());
   }

   public void testDeleteInstanceResponseIs4xx() {
      HttpRequest delete = HttpRequest
              .builder()
              .method("DELETE")
              .endpoint("https://www.googleapis" +
                      ".com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances/test-1")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse deleteResponse = HttpResponse.builder().statusCode(404).build();

      InstanceApi api = requestsSendResponses(requestForScopes(COMPUTE_SCOPE),
              TOKEN_RESPONSE, delete, deleteResponse).getInstanceApiForProject("myproject");

      assertNull(api.deleteInZone("us-central1-a", "test-1"));
   }

   public void testListInstancesResponseIs2xx() {

      InstanceApi api = requestsSendResponses(
              requestForScopes(COMPUTE_READONLY_SCOPE), TOKEN_RESPONSE,
              LIST_INSTANCES_REQUEST, LIST_INSTANCES_RESPONSE).getInstanceApiForProject("myproject");

      assertEquals(api.listFirstPageInZone("us-central1-a").toString(),
              new ParseInstanceListTest().expected().toString());
   }

   public void testListInstancesResponseIs4xx() {
      HttpRequest list = HttpRequest
              .builder()
              .method("GET")
              .endpoint("https://www.googleapis" +
                      ".com/compute/v1beta15/projects/myproject/zones/us-central1-a/instances")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(404).build();

      InstanceApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, list, operationResponse).getInstanceApiForProject("myproject");

      assertTrue(api.listInZone("us-central1-a").concat().isEmpty());
   }

}
