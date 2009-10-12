/**
 *
 * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package org.jclouds.azure.storage.blob;

import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jclouds.azure.storage.AzureBlob;
import org.jclouds.azure.storage.blob.binders.GenerateMD5AndBindBlobToEntity;
import org.jclouds.azure.storage.blob.domain.Blob;
import org.jclouds.azure.storage.blob.domain.BlobMetadata;
import org.jclouds.azure.storage.blob.domain.ContainerMetadata;
import org.jclouds.azure.storage.blob.domain.ListBlobsResponse;
import org.jclouds.azure.storage.blob.functions.ParseBlobFromHeadersAndHttpContent;
import org.jclouds.azure.storage.blob.functions.ParseBlobMetadataFromHeaders;
import org.jclouds.azure.storage.blob.functions.ReturnTrueIfContainerAlreadyExists;
import org.jclouds.azure.storage.blob.options.CreateContainerOptions;
import org.jclouds.azure.storage.blob.xml.AccountNameEnumerationResultsHandler;
import org.jclouds.azure.storage.blob.xml.AddMD5ToListBlobsResponse;
import org.jclouds.azure.storage.filters.SharedKeyAuthentication;
import org.jclouds.azure.storage.options.ListOptions;
import org.jclouds.azure.storage.reference.AzureStorageHeaders;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.functions.BlobKey;
import org.jclouds.blobstore.functions.ReturnVoidOnNotFoundOr404;
import org.jclouds.blobstore.functions.ThrowKeyNotFoundOn404;
import org.jclouds.http.functions.ParseETagHeader;
import org.jclouds.http.functions.ReturnFalseOn404;
import org.jclouds.http.options.GetOptions;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Endpoint;
import org.jclouds.rest.annotations.ExceptionParser;
import org.jclouds.rest.annotations.Headers;
import org.jclouds.rest.annotations.ParamParser;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SkipEncoding;
import org.jclouds.rest.annotations.XMLResponseParser;

/**
 * Provides access to Azure Blob via their REST API.
 * <p/>
 * All commands return a Future of the result from Azure Blob. Any exceptions incurred during
 * processing will be wrapped in an {@link ExecutionException} as documented in {@link Future#get()}.
 * 
 * @see <a href="http://msdn.microsoft.com/en-us/library/dd135733.aspx" />
 * @author Adrian Cole
 */
@SkipEncoding('/')
@RequestFilters(SharedKeyAuthentication.class)
@Headers(keys = AzureStorageHeaders.VERSION, values = "2009-07-17")
@Endpoint(AzureBlob.class)
public interface AzureBlobStore extends BlobStore<ContainerMetadata, BlobMetadata, Blob> {

   /**
    * The List Containers operation returns a list of the containers under the specified account.
    * <p />
    * The 2009-07-17 version of the List Containers operation times out after 30 seconds.
    * 
    * @param listOptions
    *           controls the number or type of results requested
    * @see ListOptions
    */
   @GET
   @XMLResponseParser(AccountNameEnumerationResultsHandler.class)
   @Path("/")
   @QueryParams(keys = "comp", values = "list")
   SortedSet<ContainerMetadata> listContainers();

   @HEAD
   @Path("{container}")
   @ExceptionParser(ReturnFalseOn404.class)
   @QueryParams(keys = "restype", values = "container")
   boolean containerExists(@PathParam("container") String container);

   /**
    * The Create Container operation creates a new container under the specified account. If the
    * container with the same name already exists, the operation fails.
    * <p/>
    * The container resource includes metadata and properties for that container. It does not
    * include a list of the blobs contained by the container.
    * 
    * @see CreateContainerOptions
    * 
    */
   @PUT
   @Path("{container}")
   @ExceptionParser(ReturnTrueIfContainerAlreadyExists.class)
   @QueryParams(keys = "restype", values = "container")
   Future<Boolean> createContainer(@PathParam("container") String container);

   /**
    * The Delete Container operation marks the specified container for deletion. The container and
    * any blobs contained within it are later deleted during garbage collection.
    * <p/>
    * When a container is deleted, a container with the same name cannot be created for at least 30
    * seconds; the container may not be available for more than 30 seconds if the service is still
    * processing the request. While the container is being deleted, attempts to create a container
    * of the same name will fail with status code 409 (Conflict), with the service returning
    * additional error information indicating that the container is being deleted. All other
    * operations, including operations on any blobs under the container, will fail with status code
    * 404 (Not Found) while the container is being deleted.
    * 
    */
   @DELETE
   @Path("{container}")
   @ExceptionParser(ReturnVoidOnNotFoundOr404.class)
   @QueryParams(keys = "restype", values = "container")
   Future<Void> deleteContainer(@PathParam("container") String container);

   @GET
   @XMLResponseParser(AddMD5ToListBlobsResponse.class)
   @Path("{container}")
   @QueryParams(keys = { "restype", "comp" }, values = { "container", "list" })
   Future<ListBlobsResponse> listBlobs(@PathParam("container") String container);

   @PUT
   @Path("{container}/{key}")
   @ResponseParser(ParseETagHeader.class)
   Future<String> putBlob(
            @PathParam("container") String container,
            @PathParam("key") @ParamParser(BlobKey.class) @BinderParam(GenerateMD5AndBindBlobToEntity.class) Blob object);

   @GET
   @ResponseParser(ParseBlobFromHeadersAndHttpContent.class)
   @ExceptionParser(ThrowKeyNotFoundOn404.class)
   @Path("{container}/{key}")
   Future<Blob> getBlob(@PathParam("container") String container, @PathParam("key") String key);

   @GET
   @ResponseParser(ParseBlobFromHeadersAndHttpContent.class)
   @ExceptionParser(ThrowKeyNotFoundOn404.class)
   @Path("{container}/{key}")
   Future<Blob> getBlob(@PathParam("container") String container, @PathParam("key") String key,
            GetOptions options);

   @GET
   @Headers(keys = "Range", values = "bytes=0-0")
   // should use HEAD, this is a hack per http://code.google.com/p/jclouds/issues/detail?id=92
   @ResponseParser(ParseBlobMetadataFromHeaders.class)
   @ExceptionParser(ThrowKeyNotFoundOn404.class)
   @Path("{container}/{key}")
   BlobMetadata blobMetadata(@PathParam("container") String container, @PathParam("key") String key);

   @DELETE
   @ExceptionParser(ReturnVoidOnNotFoundOr404.class)
   @Path("{container}/{key}")
   Future<Void> removeBlob(@PathParam("container") String container, @PathParam("key") String key);

}