package dev.v22.utils;

import com.carrotsearch.hppc.BitSet;
import dev.v22.ecs.utils.IntArray;

public class Utils {
    public static void arraySwap(byte[] arr, int a, int b) {
        byte h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static void arraySwap(short[] arr, int a, int b) {
        short h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static void arraySwap(int[] arr, int a, int b) {
        int h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static void arraySwap(long[] arr, int a, int b) {
        long h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static void arraySwap(float[] arr, int a, int b) {
        float h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static void arraySwap(double[] arr, int a, int b) {
        double h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static void arraySwap(char[] arr, int a, int b) {
        char h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static void arraySwap(boolean[] arr, int a, int b) {
        boolean h = arr[a];
        arr[a] = arr[b];
        arr[b] = h;
    }

    public static boolean arrayContains(byte[] arr, byte x) {
        for (byte s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static boolean arrayContains(short[] arr, short x) {
        for (short s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static boolean arrayContains(int[] arr, int x) {
        for (int s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static boolean arrayContains(long[] arr, long x) {
        for (long s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static boolean arrayContains(float[] arr, float x) {
        for (float s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static boolean arrayContains(double[] arr, double x) {
        for (double s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static boolean arrayContains(char[] arr, char x) {
        for (char s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static boolean arrayContains(boolean[] arr, boolean x) {
        for (boolean s : arr) {
            if (s == x) return true;
        }
        return false;
    }

    public static int nextPow2(int x) {
        if (x <= 1) return 1;
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    public static boolean containsAllBits(BitSet a, BitSet q) {
        long[] ab = a.bits;
        long[] qb = q.bits;

        int max = Math.min(ab.length, qb.length);
        for (int i = 0; i < max; i++) {
            long qw = qb[i];
            if ((ab[i] & qw) != qw) return false;
        }

        for (int i = max; i < qb.length; i++) {
            if (qb[i] != 0L) return false;
        }

        return true;
    }

}
