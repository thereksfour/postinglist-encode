package com.bs.lucene.codec.VByteCodec;


import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;


public class VByteCodec extends FilterCodec {
    private final PostingsFormat postings = new SimpleTextPostingsFormat();
    public  VByteCodec() {
        super("VByteCodec", new Lucene80Codec());
    }

    @Override
    public PostingsFormat postingsFormat() {
        return postings;
    }
}



