package test;

import src.SDES_;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class BruteForceTest {
    static ArrayList<int []> plaintext_list = new ArrayList<>();
    static ArrayList<int []> ciphertext_list = new ArrayList<>();
    static ArrayList<int []> key_list = new ArrayList<>();
    static ArrayList<int []> key_list1 = new ArrayList<>();
    static SDES_ sdes = new SDES_();

    public static int [] bruteForceGetKey(int[] plaintext, int[] ciphertext){
        //在已经知道明文和密文的情况下，采用暴力破解的方法来获得密钥，因为密钥为10位二进制数，暴力破解只需要从0到1023共1024种情况
        for (int i = 0; i < 1024; i++) {
            StringBuilder binaryKey = new StringBuilder(Integer.toBinaryString(i));
            while (binaryKey.length() < 10) {
                // 高位补0，确保密钥长度为10位
                binaryKey.insert(0, "0");
            }
            int [] key_test = toIntArray(String.valueOf(binaryKey));
            int [] ciphertext_test = sdes.encrypt(key_test, plaintext);
            //System.out.println(Arrays.toString(key_test));
            if (isEqual(ciphertext, ciphertext_test)) return key_test;

        }
        return null;
    }
    //使用多线程的方法来提高破解效率
    public static int [] bruteForceGetKeyMultiThread(int[] plaintext, int[] ciphertext){
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 创建一个包含10个线程的线程池

        // 提交暴力破解任务
        Future<int[]> future = executorService.submit(() -> bruteForceGetKey(plaintext, ciphertext));

        // 在完成时获取结果
        try {
            int[] key = future.get();
            if (key != null) {
                //System.out.println("找到密钥: " + Arrays.toString(key));
                return key;
            } else {
                System.out.println("未找到密钥");
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown(); // 关闭线程池
        }
        return null;
    }

    //寻找一组明密文对可能存在的多组密钥
    public static List<int []> bruteForceGetMoreKey(int [] plaintext, int [] ciphertext) {
        List<int []> key_list = new ArrayList<>();
        for (int i = 0; i < 1024; i++) {
            StringBuilder binaryKey = new StringBuilder(Integer.toBinaryString(i));
            while (binaryKey.length() < 10) {
                // 高位补0，确保密钥长度为10位
                binaryKey.insert(0, "0");
            }
            int [] key_test = toIntArray(String.valueOf(binaryKey));
            int [] ciphertext_test = sdes.encrypt(key_test, plaintext);
            //System.out.println(Arrays.toString(key_test));
            if (isEqual(ciphertext, ciphertext_test)) key_list.add(key_test);
        }
        return key_list;
    }

    public static void main(String[] args) {
        //分别向两个列表中加入8位二进制的明文和密文
        plaintext_list.add(new int[]{1, 1, 1, 1, 1, 1, 1, 1});
        ciphertext_list.add(new int[]{1, 1, 1, 1, 1, 1, 1, 1});

        plaintext_list.add(new int[]{0, 0, 0, 0, 0, 0, 0, 0});
        ciphertext_list.add(new int[]{0, 0, 0, 0, 0, 0, 0, 0});

        plaintext_list.add(new int[]{1, 0, 1, 0, 1, 0, 1, 0});
        ciphertext_list.add(new int[]{1, 0, 1, 0, 1, 0, 1, 0});

        plaintext_list.add(new int[]{0, 1, 0, 1, 0, 1, 0, 1});
        ciphertext_list.add(new int[]{0, 1, 0, 1, 0, 1, 0, 1});

        plaintext_list.add(new int[]{1, 1, 0, 0, 1, 1, 0, 0});
        ciphertext_list.add(new int[]{1, 1, 0, 0, 1, 1, 0, 0});

        plaintext_list.add(new int[]{1, 0, 0, 1, 1, 0, 1, 0});
        ciphertext_list.add(new int[]{1, 1, 1, 0, 1, 1, 1, 1});
        for (int i = 1; i <= plaintext_list.size(); i++) {
            System.out.println("第"+i+"组明文和密文：");
            System.out.println(Arrays.toString(plaintext_list.get(i - 1)));
            System.out.println(Arrays.toString(ciphertext_list.get(i - 1)));
        }

        //调用暴力破解的方法，并记录时间
        ArrayList<Long> time_list = new ArrayList<>();
        ArrayList<Long> time_list1 = new ArrayList<>();
        for (int i = 0; i < plaintext_list.size(); i++) {
            long startTime = System.nanoTime();
            int[] key = bruteForceGetKey(plaintext_list.get(i), ciphertext_list.get(i));
            long endTime = System.nanoTime();
            long elapsedTime = endTime - startTime;
            key_list.add(key);
            time_list.add(elapsedTime/1000);
        }
        for (int i = 0; i < plaintext_list.size(); i++) {
            long startTime = System.nanoTime();
            int[] key = bruteForceGetKeyMultiThread(plaintext_list.get(i), ciphertext_list.get(i));
            long endTime = System.nanoTime();
            long elapsedTime = endTime - startTime;
            key_list1.add(key);
            time_list1.add(elapsedTime/1000);
        }
        System.out.println("暴力破解密钥的时间消耗(微秒)：" + time_list);
        System.out.println("密钥分别为：");
        for (int[] ints : key_list) {
            System.out.println(Arrays.toString(ints));
        }
        System.out.println("多线程暴力破解消耗的时间（微秒）为："+time_list1);
        System.out.println("密钥分别为：");
        for (int[] ints : key_list1) {
            System.out.println(Arrays.toString(ints));
        }
        for (int i = 0; i < plaintext_list.size(); i++) {
            List<int []> keys_list = bruteForceGetMoreKey(plaintext_list.get(i),ciphertext_list.get(i));
            System.out.println("第"+i+"对明密文破解出来的密钥有：");
            for (int[] ints : keys_list) {
                System.out.println(Arrays.toString(ints));
            }
        }

    }
    public static int[] toIntArray(String str) {
        char[] chars = str.toCharArray();
        int length = chars.length;
        int[] ints = new int[length];
        for (int i = 0; i < length; i++) {
            ints[i] = chars[i] - '0';
        }
        return ints;
    }
    public static boolean isEqual(int [] A,int [] B){
        if (A.length != B.length) return false;
        //判断两个数组是否相等
        for (int i = 0; i < A.length; i++) {
            if (A[i]!= B[i]) return  false;
        }
        return true;
    }
}
