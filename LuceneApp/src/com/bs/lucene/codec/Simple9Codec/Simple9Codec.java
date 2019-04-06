package com.bs.lucene.codec.Simple9Codec;


import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;


public class Simple9Codec extends FilterCodec {
    private final PostingsFormat postings = new SimpleTextPostingsFormat();
    public  Simple9Codec() {
        super("Simple9Codec", new Lucene80Codec());
    }

    @Override
    public PostingsFormat postingsFormat() {
        return postings;
    }
}



