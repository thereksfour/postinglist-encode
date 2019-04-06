package com.bs.lucene.codec.algorithm;


import com.kamikaze.pfordelta.PForDelta;

public class MyPForDelta {
    public MyPForDelta(){

    }
    public int[] PFDEncode(int[] intArr){

        PForDelta PFD = new PForDelta();
        //System.out.println("PFD before compress: "+ Arrays.toString(intArr));
        int[] tmpArr = PFD.compressOneBlockOpt(intArr,intArr.length);
        //System.out.println("PFD after compress: "+ Arrays.toString(tmpArr));

        int[] outArr = new int[tmpArr.length+1];
        for(int i=0;i<tmpArr.length;i++){
            outArr[i] = tmpArr[i];
        }
        outArr[tmpArr.length] = intArr.length;
        //System.out.println("s9 after compress: "+ Arrays.toString(outArr));
        return outArr;
    }
    public int[] PFDDecode(int[] intArr){

        //System.out.println("PFD uncompress: "+ Arrays.toString(intArr));
        int[] outArr = new int[intArr[intArr.length-1]];
        int[] inArr = new int[intArr.length-1];
        for(int i=0;i<inArr.length;i++){
            inArr[i] = intArr[i];
        }
        PForDelta PFD = new PForDelta();
        PFD.decompressOneBlock(outArr,inArr,outArr.length);
        return outArr;
    }
}