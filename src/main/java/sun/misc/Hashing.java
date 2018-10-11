/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package sun.misc;

import java.util.Random;

/**
 * Hashing utilities.
 *
 * Little endian implementations of Murmur3 hashing.
 */
public class Hashing {

    /**
     * Static utility methods only.
     */
    private Hashing() {
        throw new Error("No instances");
    }

    public static int murmur3_32(byte[] data) {
        return murmur3_32(0, data, 0, data.length);
    }

    public static int murmur3_32(int seed, byte[] data) {
        return murmur3_32(seed, data, 0, data.length);
    }

    @SuppressWarnings("fallthrough")
    public static int murmur3_32(int seed, byte[] data, int offset, int len) {
        int h1 = seed;
        int count = len;

        // body
        while (count >= 4) {
            int k1 = (data[offset] & 0x0FF)
                    | (data[offset + 1] & 0x0FF) << 8
                    | (data[offset + 2] & 0x0FF) << 16
                    | data[offset + 3] << 24;

            count -= 4;
            offset += 4;

            k1 *= 0xcc9e2d51;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= 0x1b873593;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // tail

        if (count > 0) {
            int k1 = 0;

            switch (count) {
                case 3:
                    k1 ^= (data[offset + 2] & 0xff) << 16;
                // fall through
                case 2:
                    k1 ^= (data[offset + 1] & 0xff) << 8;
                // fall through
                case 1:
                    k1 ^= (data[offset] & 0xff);
                // fall through
                default:
                    k1 *= 0xcc9e2d51;
                    k1 = Integer.rotateLeft(k1, 15);
                    k1 *= 0x1b873593;
                    h1 ^= k1;
            }
        }

        // finalization

        h1 ^= len;

        // finalization mix force all bits of a hash block to avalanche
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }

    public static int murmur3_32(char[] data) {
        return murmur3_32(0, data, 0, data.length);
    }

    public static int murmur3_32(int seed, char[] data) {
        return murmur3_32(seed, data, 0, data.length);
    }

    public static int murmur3_32(int seed, char[] data, int offset, int len) {
        int h1 = seed;

        int off = offset;
        int count = len;

        // body
        while (count >= 2) {
            int k1 = (data[off++] & 0xFFFF) | (data[off++] << 16);

            count -= 2;

            k1 *= 0xcc9e2d51;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= 0x1b873593;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // tail

        if (count > 0) {
            int k1 = data[off];

            k1 *= 0xcc9e2d51;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= 0x1b873593;
            h1 ^= k1;
        }

        // finalization

        h1 ^= len * (Character.SIZE / Byte.SIZE);

        // finalization mix force all bits of a hash block to avalanche
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }

    public static int murmur3_32(int[] data) {
        return murmur3_32(0, data, 0, data.length);
    }

    public static int murmur3_32(int seed, int[] data) {
        return murmur3_32(seed, data, 0, data.length);
    }

    public static int murmur3_32(int seed, int[] data, int offset, int len) {
        int h1 = seed;

        int off = offset;
        int end = offset + len;

        // body
        while (off < end) {
            int k1 = data[off++];

            k1 *= 0xcc9e2d51;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= 0x1b873593;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // tail (always empty, as body is always 32-bit chunks)

        // finalization

        h1 ^= len * (Integer.SIZE / Byte.SIZE);

        // finalization mix force all bits of a hash block to avalanche
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }

    /**
     * Holds references to things that can't be initialized until after VM
     * is fully booted.
     */
    private static class Holder {

        /**
         * Used for generating per-instance hash seeds.
         *
         * We try to improve upon the default seeding.
         */
        static final Random SEED_MAKER = new Random(
                Double.doubleToRawLongBits(Math.random())
                ^ System.identityHashCode(Hashing.class)
                ^ System.currentTimeMillis()
                ^ System.nanoTime()
                ^ Runtime.getRuntime().freeMemory());

        /**
         * Access to {@code String.hash32()}
         */
        static final JavaLangAccess LANG_ACCESS;

        static {
            LANG_ACCESS = SharedSecrets.getJavaLangAccess();
            if (null == LANG_ACCESS) {
                throw new Error("Shared secrets not initialized");
            }
        }
    }

    /**
     * Return a 32 bit hash value for the specified string. The algorithm is
     * unspecified but will be consistent within a VM instance.
     *
     * @param string String to be hashed.
     * @return hash value of the string.
     */
    public static int stringHash32(String string) {
        return Holder.LANG_ACCESS.getStringHash32(string);
    }

    public static int randomHashSeed(Object instance) {
        int seed;
        if (sun.misc.VM.isBooted()) {
            seed = Holder.SEED_MAKER.nextInt();
        } else {
            // lower quality "random" seed value--still better than zero and not
            // not practically reversible.
            int hashing_seed[] = {
                System.identityHashCode(Hashing.class),
                System.identityHashCode(instance),
                System.identityHashCode(Thread.currentThread()),
                (int) Thread.currentThread().getId(),
                (int) (System.currentTimeMillis() >>> 2), // resolution is poor
                (int) (System.nanoTime() >>> 5), // resolution is poor
                (int) (Runtime.getRuntime().freeMemory() >>> 4) // alloc min
            };

            seed = murmur3_32(hashing_seed);
        }

        // force to non-zero.
        return (0 != seed) ? seed : 1;
    }
}
