package com.bs.lucene.codec.algorithm;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.VariableByte;

import java.util.Arrays;

public class MyVByte {
    public MyVByte(){

    }
    public int[] VByteEncode(int[] intArr){
        IntWrapper in = new IntWrapper(0);
        IntWrapper out = new IntWrapper(0);
        int[] tmpArr = new int[intArr.length+2];
        VariableByte vb = new VariableByte();
        //System.out.println("vb compress: "+ Arrays.toString(intArr));
        vb.compress(intArr,in,intArr.length,tmpArr,out);
        int[] outArr = new int[out.get()+1];
        for(int i=0;i<outArr.length;i++){
            outArr[i] = tmpArr[i];
        }
        outArr[out.get()] = intArr.length;
        //System.out.println("vb uncompress: "+ Arrays.toString(outArr));

        return outArr;
    }
    public int[] VByteDecode(int[] intArr){
        IntWrapper in = new IntWrapper(0);
        IntWrapper out = new IntWrapper(0);
        //System.out.println("vb uncompress: "+ Arrays.toString(intArr));
        int[] outArr = new int[intArr[intArr.length-1]];
        VariableByte vb = new VariableByte();
        vb.uncompress(intArr,in,intArr.length-1,outArr,out);
        return outArr;
    }
}
