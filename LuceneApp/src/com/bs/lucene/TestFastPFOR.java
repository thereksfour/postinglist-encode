//package com.bs.lucene;
//
//import me.lemire.integercompression.IntWrapper;
//import me.lemire.integercompression.*;
//import com.kamikaze.pfordelta.PForDelta;
//
//import java.util.Arrays;
//
//public class TestFastPFOR {
//    public static void main(String args[]) {
//        int[] plainArr = {0,1,2,3,4,5,118,7,8,10};
//        PForDelta codec = new PForDelta();
//        System.out.println("Plain: "+Arrays.toString(plainArr));
//        int[] compressArr = codec.compressOneBlockOpt(plainArr,8);
//        System.out.println("Compress: "+Arrays.toString(compressArr));
//        int[] uncompressArr = new int[plainArr.length];
//        codec.decompressOneBlock(uncompressArr,compressArr,8);
//        System.out.println("Uncompress: "+Arrays.toString(uncompressArr));
//
//
//    }
//
//}
