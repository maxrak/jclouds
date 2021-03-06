/**
 *
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.ec2.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.jclouds.util.Preconditions2;

/**
 * 
 * @author Lili Nadar
 */
public class BlockDeviceMapping {
   private final String deviceName;
   private final String virtualName;
   private final String snapshotId;
   private final Integer sizeInGib;
   private final Boolean noDevice;
   private final Boolean deleteOnTermination;

   // values expressed in GB
   private static final Integer VOLUME_SIZE_MIN_VALUE = 1;
   private static final Integer VOLUME_SIZE_MAX_VALUE = 1000;

   public BlockDeviceMapping(String deviceName, @Nullable String virtualName, @Nullable String snapshotId,
         @Nullable Integer sizeInGib, @Nullable Boolean noDevice, @Nullable Boolean deleteOnTermination) {

      checkNotNull(deviceName, "deviceName cannot be null");
      Preconditions2.checkNotEmpty(deviceName, "the deviceName must be non-empty");

      if (sizeInGib != null) {
         checkArgument((sizeInGib >= VOLUME_SIZE_MIN_VALUE && sizeInGib <= VOLUME_SIZE_MAX_VALUE),
               String.format("Size in Gib must be between %s and %s GB", VOLUME_SIZE_MIN_VALUE, VOLUME_SIZE_MAX_VALUE));
      }
      this.deviceName = deviceName;
      this.virtualName = virtualName;
      this.snapshotId = snapshotId;
      this.sizeInGib = sizeInGib;
      this.noDevice = noDevice;
      this.deleteOnTermination = deleteOnTermination;
   }

   public String getDeviceName() {
      return deviceName;
   }

   public String getVirtualName() {
      return virtualName;
   }

   public String getEbsSnapshotId() {
      return snapshotId;
   }

   public Integer getEbsVolumeSize() {
      return sizeInGib;
   }

   public Boolean getEbsNoDevice() {
      return noDevice;
   }

   public Boolean getEbsDeleteOnTermination() {
      return deleteOnTermination;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      BlockDeviceMapping other = (BlockDeviceMapping) obj;
      if (deviceName == null) {
         if (other.deviceName != null)
            return false;
      } else if (!deviceName.equals(other.deviceName))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "BlockDeviceMapping [deviceName=" + deviceName + ", virtualName=" + virtualName + ", snapshotId="
            + snapshotId + ", sizeInGib=" + sizeInGib + ", noDevice=" + noDevice + ", deleteOnTermination="
            + deleteOnTermination + "]";
   }

   public static class MapEBSSnapshotToDevice extends BlockDeviceMapping {
      public MapEBSSnapshotToDevice(String deviceName, String snapshotId, @Nullable Integer sizeInGib,
            @Nullable Boolean deleteOnTermination) {
         super(deviceName, null, snapshotId, sizeInGib, null, deleteOnTermination);
         checkNotNull(snapshotId, "snapshotId cannot be null");
         Preconditions2.checkNotEmpty(snapshotId, "the snapshotId must be non-empty");
      }
   }

   public static class MapNewVolumeToDevice extends BlockDeviceMapping {
      public MapNewVolumeToDevice(String deviceName, Integer sizeInGib, @Nullable Boolean deleteOnTermination) {
         super(deviceName, null, null, sizeInGib, null, deleteOnTermination);
         checkNotNull(sizeInGib, "sizeInGib cannot be null");
      }
   }

   public static class MapEphemeralDeviceToDevice extends BlockDeviceMapping {
      public MapEphemeralDeviceToDevice(String deviceName, String virtualName) {
         super(deviceName, virtualName, null, null, null, null);
         checkNotNull(virtualName, "virtualName cannot be null");
         Preconditions2.checkNotEmpty(virtualName, "the virtualName must be non-empty");
      }
   }

   public static class UnmapDeviceNamed extends BlockDeviceMapping {
      public UnmapDeviceNamed(String deviceName) {
         super(deviceName, null, null, null, true, null);
      }
   }
}