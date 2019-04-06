package com.bs.lucene.codec.algorithm;

import me.lemire.integercompression.Delta;

import java.util.Arrays;

public class DGap {
    private Delta delta = new Delta();
    public DGap(){

    }
    public int[] DGapEncode(int[] intArr){
        //System.out.println("DGap compress start: "+ Arrays.toString(intArr));
        delta.delta(intArr);
        //System.out.println("DGap compress: "+ Arrays.toString(intArr));
        return intArr;
    }
    public int[] DGapDecode(int[] intArr){
        //System.out.println("DGap uncompress start: "+ Arrays.toString(intArr));

        delta.inverseDelta(intArr);
        //System.out.println("DGap uncompress : "+ Arrays.toString(intArr));

        return intArr;
    }
}
