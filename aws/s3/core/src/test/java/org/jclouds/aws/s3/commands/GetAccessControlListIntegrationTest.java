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
package org.jclouds.aws.s3.commands;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jclouds.aws.s3.S3IntegrationTest;
import org.jclouds.aws.s3.commands.options.PutObjectOptions;
import org.jclouds.aws.s3.domain.AccessControlList;
import org.jclouds.aws.s3.domain.S3Object;
import org.jclouds.aws.s3.domain.AccessControlList.GroupGranteeURI;
import org.jclouds.aws.s3.domain.AccessControlList.Permission;
import org.jclouds.aws.s3.domain.acl.CannedAccessPolicy;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * Tests integrated functionality of all commands that retrieve Access Control Lists (ACLs).
 *
 * @author James Murty
 */
@Test(groups = {"integration", "live"}, testName = "s3.GetAccessControlListIntegrationTest")
public class GetAccessControlListIntegrationTest extends S3IntegrationTest {

   @Test
   void testPrivateBucketACL() throws InterruptedException, ExecutionException, 
         TimeoutException, IOException 
   {
      bucketName = bucketPrefix + ".testPrivateBucketACL".toLowerCase();
         
      createBucketAndEnsureEmpty(bucketName);
      
      AccessControlList acl = client.getBucketACL(bucketName).get(10, TimeUnit.SECONDS);
      
      assertEquals(acl.getGrants().size(), 1);
      assertTrue(acl.getOwner() != null);
      String ownerId = acl.getOwner().getId();
      assertTrue(acl.hasPermission(ownerId, Permission.FULL_CONTROL));
   }

   @Test
   void testObjectACL() throws InterruptedException, ExecutionException, 
         TimeoutException, IOException 
   {
      bucketName = bucketPrefix + ".testObjectACL".toLowerCase();
      createBucketAndEnsureEmpty(bucketName);

      String privateObjectKey = "pr�vate-acl";
      String publicReadObjectKey = "p�blic-read-acl";         
      String publicReadWriteObjectKey = "p�blic-read-write-acl";         
      
      // Private object
      addObjectToBucket(bucketName, privateObjectKey);      
      AccessControlList acl = client.getObjectACL(bucketName, privateObjectKey)
         .get(10, TimeUnit.SECONDS);
      
      assertEquals(acl.getGrants().size(), 1);
      assertTrue(acl.getOwner() != null);
      String ownerId = acl.getOwner().getId();
      assertTrue(acl.hasPermission(ownerId, Permission.FULL_CONTROL));

      // Public Read object
      client.putObject(bucketName, new S3Object(publicReadObjectKey, ""), 
            new PutObjectOptions().withAcl(CannedAccessPolicy.PUBLIC_READ));
      
      acl = client.getObjectACL(bucketName, publicReadObjectKey)
         .get(10, TimeUnit.SECONDS);
      
      assertEquals(acl.getGrants().size(), 2);
      assertEquals(acl.getPermissions(GroupGranteeURI.ALL_USERS).size(), 1);
      assertTrue(acl.getOwner() != null);
      ownerId = acl.getOwner().getId();
      assertTrue(acl.hasPermission(ownerId, Permission.FULL_CONTROL));
      assertTrue(acl.hasPermission(GroupGranteeURI.ALL_USERS, Permission.READ));

      // Public Read-Write object
      client.putObject(bucketName, new S3Object(publicReadWriteObjectKey, ""), 
            new PutObjectOptions().withAcl(CannedAccessPolicy.PUBLIC_READ_WRITE));
      
      acl = client.getObjectACL(bucketName, publicReadWriteObjectKey)
         .get(10, TimeUnit.SECONDS);
      
      assertEquals(acl.getGrants().size(), 3);
      assertEquals(acl.getPermissions(GroupGranteeURI.ALL_USERS).size(), 2);
      assertTrue(acl.getOwner() != null);
      ownerId = acl.getOwner().getId();
      assertTrue(acl.hasPermission(ownerId, Permission.FULL_CONTROL));
      assertTrue(acl.hasPermission(GroupGranteeURI.ALL_USERS, Permission.READ));
      assertTrue(acl.hasPermission(GroupGranteeURI.ALL_USERS, Permission.WRITE));
      assertFalse(acl.hasPermission(GroupGranteeURI.ALL_USERS, Permission.READ_ACP));
      assertFalse(acl.hasPermission(GroupGranteeURI.ALL_USERS, Permission.WRITE_ACP));
      assertFalse(acl.hasPermission(GroupGranteeURI.ALL_USERS, Permission.FULL_CONTROL));

      emptyBucket(bucketName);
   }

}