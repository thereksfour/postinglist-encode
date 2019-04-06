package com.bs.lucene.codec.algorithm;

import me.lemire.integercompression.Simple9;
import me.lemire.integercompression.IntWrapper;

import java.util.Arrays;

public class MySimple9 {

    public MySimple9(){
    }

    public int[] Simple9Encode(int[] intArr){
        IntWrapper in = new IntWrapper(0);
        IntWrapper out = new IntWrapper(0);
        int[] tmpArr = new int[intArr.length+2];
        Simple9 s9 = new Simple9();
        //System.out.println("s9 before compress: "+ Arrays.toString(intArr));
        s9.compress(intArr,in,intArr.length,tmpArr,out);
        //System.out.println("s9 after compress: "+ Arrays.toString(tmpArr));

        int[] outArr = new int[out.get()+1];
        for(int i=0;i<outArr.length;i++){
            outArr[i] = tmpArr[i];
        }
        outArr[out.get()] = intArr.length;
        //System.out.println("s9 after compress: "+ Arrays.toString(outArr));
        return outArr;
    }
    public int[] Simple9Decode(int[] intArr){
        IntWrapper in = new IntWrapper(0);
        IntWrapper out = new IntWrapper(0);
        //System.out.println("s9 uncompress: "+ Arrays.toString(intArr));
        int[] outArr = new int[intArr[intArr.length-1]];
        Simple9 s9 = new Simple9();
        s9.uncompress(intArr,in,intArr.length-1,outArr,out);
        return outArr;
    }

}