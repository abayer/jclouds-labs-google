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

import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.COMPUTE_READONLY_SCOPE;
import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.COMPUTE_SCOPE;
import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.STORAGE_WRITEONLY_SCOPE;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyIterableWithMarkerOnNotFoundOr404;
import org.jclouds.Fallbacks.EmptyPagedIterableOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.collect.PagedIterable;
import org.jclouds.googlecomputeengine.domain.Image;
import org.jclouds.googlecomputeengine.domain.ListPage;
import org.jclouds.googlecomputeengine.domain.Operation;
import org.jclouds.googlecomputeengine.functions.internal.ParseImages;
import org.jclouds.googlecomputeengine.options.DeprecateOptions;
import org.jclouds.googlecomputeengine.options.ListOptions;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.config.OAuthScopes;
import org.jclouds.oauth.v2.filters.OAuthAuthenticator;
import org.jclouds.rest.annotations.*;
import org.jclouds.rest.binders.BindToJsonPayload;

/**
 * Provides access to Images via their REST API.
 * <p/>
 *
 * @author David Alves
 * @see <a href="https://developers.google.com/compute/docs/reference/v1beta15/images"/>
 */
@SkipEncoding({'/', '='})
@RequestFilters(OAuthAuthenticator.class)
public interface ImageApi {
   /**
    * Returns the specified image resource.
    *
    * @param imageName name of the image resource to return.
    * @return an Image resource
    */
   @Named("Images:get")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images/{image}")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Image get(@PathParam("image") String imageName);

   /**
    * Deletes the specified image resource.
    *
    * @param imageName name of the image resource to delete.
    * @return an Operation resource. To check on the status of an operation, poll the Operations resource returned to
    *         you, and look for the status field.  If the image did not exist the result is null.
    */
   @Named("Images:delete")
   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images/{image}")
   @OAuthScopes(COMPUTE_SCOPE)
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Operation delete(@PathParam("image") String imageName);

   /**
    * @see ImageApi#listAtMarker(String, org.jclouds.googlecomputeengine.options.ListOptions)
    */
   @Named("Images:list")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseImages.class)
   @Fallback(EmptyIterableWithMarkerOnNotFoundOr404.class)
   ListPage<Image> listFirstPage();

   /**
    * @see ImageApi#listAtMarker(String, org.jclouds.googlecomputeengine.options.ListOptions)
    */
   @Named("Images:list")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseImages.class)
   @Fallback(EmptyIterableWithMarkerOnNotFoundOr404.class)
   ListPage<Image> listAtMarker(@QueryParam("pageToken") @Nullable String marker);

   /**
    * Retrieves the list of image resources available to the specified project.
    * By default the list as a maximum size of 100, if no options are provided or ListOptions#getMaxResults() has not
    * been set.
    *
    * @param marker      marks the beginning of the next list page
    * @param listOptions listing options
    * @return a page of the list
    * @see ListOptions
    * @see org.jclouds.googlecomputeengine.domain.ListPage
    */
   @Named("Images:list")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseImages.class)
   @Fallback(EmptyIterableWithMarkerOnNotFoundOr404.class)
   ListPage<Image> listAtMarker(@QueryParam("pageToken") @Nullable String marker, ListOptions listOptions);

   /**
    * A paged version of ImageApi#list()
    *
    * @return a Paged, Fluent Iterable that is able to fetch additional pages when required
    * @see PagedIterable
    * @see ImageApi#listAtMarker(String, org.jclouds.googlecomputeengine.options.ListOptions)
    */
   @Named("Images:list")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseImages.class)
   @Transform(ParseImages.ToPagedIterable.class)
   @Fallback(EmptyPagedIterableOnNotFoundOr404.class)
   PagedIterable<Image> list();

   /**
    * A paged version of ImageApi#list()
    *
    * @return a Paged, Fluent Iterable that is able to fetch additional pages when required
    * @see PagedIterable
    * @see ImageApi#listAtMarker(String, org.jclouds.googlecomputeengine.options.ListOptions)
    */
   @Named("Images:list")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseImages.class)
   @Transform(ParseImages.ToPagedIterable.class)
   @Fallback(EmptyPagedIterableOnNotFoundOr404.class)
   PagedIterable<Image> list(ListOptions options);

   /**
    * TODO live and expect tests for deprecate
    * Set the deprecation status of an image.
    *
    * @param image The name of the image
    * @param deprecateOptions Optional DeprecateOptions object - if null, the deprecation status will be cleared
    *
    * @return an Operation resource. To check on the status of an operation, poll the Operations resource returned to
    *         you, and look for the status field.  If the image did not exist the result is null.
    */
   @Named("Images:deprecate")
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/global/images/{image}/deprecate")
   @OAuthScopes(COMPUTE_SCOPE)
   Operation deprecate(@PathParam("image") String image,
                       @BinderParam(BindToJsonPayload.class) DeprecateOptions deprecateOptions);

   /**
    * TODO live and expect tests for insert
    * Create a new image from an existing disk image.
    *
    * @param name the name for the new image.
    * @param preferredKernel the URL for the {@link org.jclouds.googlecomputeengine.domain.Kernel} to use for this image.
    * @param source URL for raw disk source.
    * @param sourceType Source type of image. Must be RAW.
    *
    * @return an Operation resource. To check on the status of an operation, poll the Operations resource returned to
    *         you, and look for the status field.
    */
   @Named("Images:insert")
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/global/images")
   @OAuthScopes({COMPUTE_SCOPE, STORAGE_WRITEONLY_SCOPE})
   @MapBinder(BindToJsonPayload.class)
   Operation create(@PayloadParam("name") String name,
                    @PayloadParam("preferredKernel") String preferredKernel,
                    @WrapWith("rawDisk") @PayloadParam("source") String source,
                    @PayloadParam("sourceType") String sourceType);

}
