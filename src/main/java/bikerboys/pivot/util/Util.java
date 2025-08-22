package bikerboys.pivot.util;

import bikerboys.pivot.Pivot;
import bikerboys.pivot.attachmenttype.LockedCustomAttachedData;
import bikerboys.pivot.attachmenttype.UUIDCustomAttachedData;

import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import org.joml.Quaternionf;

import java.util.UUID;

public class Util {


    public static Quaternionf eulerDegreesToQuaternion(float degX, float degY, float degZ) {
        // half-angles in radians
        double rx = Math.toRadians(degX) * 0.5;
        double ry = Math.toRadians(degY) * 0.5;
        double rz = Math.toRadians(degZ) * 0.5;

        float sx = (float)Math.sin(rx), cx = (float)Math.cos(rx);
        float sy = (float)Math.sin(ry), cy = (float)Math.cos(ry);
        float sz = (float)Math.sin(rz), cz = (float)Math.cos(rz);

        // quaternions for each axis: qx, qy, qz (x,y,z,w)
        float qx_x = sx, qx_y = 0f, qx_z = 0f, qx_w = cx;
        float qy_x = 0f, qy_y = sy, qy_z = 0f, qy_w = cy;
        float qz_x = 0f, qz_y = 0f, qz_z = sz, qz_w = cz;

        // multiply q = qz * qy * qx  (apply X then Y then Z)
        Quaternionf qxQ = new Quaternionf(qx_x, qx_y, qx_z, qx_w);
        Quaternionf qyQ = new Quaternionf(qy_x, qy_y, qy_z, qy_w);
        Quaternionf qzQ = new Quaternionf(qz_x, qz_y, qz_z, qz_w);

        // explicit Hamilton product to avoid ambiguity about library multiply order:
        Quaternionf tmp = hamiltonProduct(qyQ, qxQ); // qy * qx
        Quaternionf q   = hamiltonProduct(qzQ, tmp); // qz * (qy * qx)
        return q;
    }

    public static Quaternionf hamiltonProduct(Quaternionf a, Quaternionf b) {
        // a=(ax,ay,az,aw), b=(bx,by,bz,bw)
        float ax = a.x, ay = a.y, az = a.z, aw = a.w;
        float bx = b.x, by = b.y, bz = b.z, bw = b.w;
        float x = aw*bx + ax*bw + ay*bz - az*by;
        float y = aw*by - ax*bz + ay*bw + az*bx;
        float z = aw*bz + ax*by - ay*bx + az*bw;
        float w = aw*bw - ax*bx - ay*by - az*bz;
        return new Quaternionf(x, y, z, w);
    }


    public static String getUuidFromDisplayEntity(DisplayEntity displayEntity) {

        UUIDCustomAttachedData attachedOrCreate = displayEntity.getAttachedOrCreate(Pivot.UUID_ATTACHMENT);

        return attachedOrCreate.uuid();
    }

    public static void setUuidDisplayEntity(DisplayEntity displayEntity, String string) {
        UUIDCustomAttachedData attachedOrCreate = displayEntity.getAttachedOrCreate(Pivot.UUID_ATTACHMENT);

       displayEntity.setAttached(Pivot.UUID_ATTACHMENT, attachedOrCreate.setUuid(string));
    }

    public static void setUuidDisplayEntity(DisplayEntity displayEntity, PlayerEntity player) {
        UUIDCustomAttachedData attachedOrCreate = displayEntity.getAttachedOrCreate(Pivot.UUID_ATTACHMENT);

        displayEntity.setAttached(Pivot.UUID_ATTACHMENT, attachedOrCreate.setUuid(player.getUuidAsString()));
    }

    public static void setUuidDisplayEntity(DisplayEntity displayEntity, ServerPlayerEntity player) {
        UUIDCustomAttachedData attachedOrCreate = displayEntity.getAttachedOrCreate(Pivot.UUID_ATTACHMENT);

        displayEntity.setAttached(Pivot.UUID_ATTACHMENT, attachedOrCreate.setUuid(player.getUuidAsString()));
    }


    public static void setLockedDisplayEntity(DisplayEntity displayEntity) {
        LockedCustomAttachedData attachedOrCreate = displayEntity.getAttachedOrCreate(Pivot.LOCKED_ATTACHMENT);

        displayEntity.setAttached(Pivot.LOCKED_ATTACHMENT, attachedOrCreate.setLocked(true));
    }

    public static void setLockedDisplayEntity(DisplayEntity displayEntity, boolean b) {
        LockedCustomAttachedData attachedOrCreate = displayEntity.getAttachedOrCreate(Pivot.LOCKED_ATTACHMENT);

        displayEntity.setAttached(Pivot.LOCKED_ATTACHMENT, attachedOrCreate.setLocked(b));
    }

    public static boolean getLockedDisplayEntity(DisplayEntity displayEntity) {
        return displayEntity.getAttachedOrCreate(Pivot.LOCKED_ATTACHMENT).locked();
    }


    public static void setUnlockedDisplayEntity(DisplayEntity displayEntity) {
        LockedCustomAttachedData attachedOrCreate = displayEntity.getAttachedOrCreate(Pivot.LOCKED_ATTACHMENT);

        displayEntity.setAttached(Pivot.LOCKED_ATTACHMENT, attachedOrCreate.setLocked(false));
    }







    public static float[] quaternionToEulerDegrees(Quaternionf q) {
        // copy & normalize to avoid mutating the original
        Quaternionf qq = new Quaternionf(q).normalize();

        double x = qq.x();
        double y = qq.y();
        double z = qq.z();
        double w = qq.w();

        // X (roll)
        double t0 = 2.0 * (w * x + y * z);
        double t1 = 1.0 - 2.0 * (x * x + y * y);
        double rotX = Math.toDegrees(Math.atan2(t0, t1));

        // Y (pitch) -- clamp to avoid NaN from rounding errors
        double t2 = 2.0 * (w * y - z * x);
        t2 = Math.max(-1.0, Math.min(1.0, t2));
        double rotY = Math.toDegrees(Math.asin(t2));

        // Z (yaw)
        double t3 = 2.0 * (w * z + x * y);
        double t4 = 1.0 - 2.0 * (y * y + z * z);
        double rotZ = Math.toDegrees(Math.atan2(t3, t4));

        return new float[] { (float)rotX, (float)rotY, (float)rotZ };
    }

}
