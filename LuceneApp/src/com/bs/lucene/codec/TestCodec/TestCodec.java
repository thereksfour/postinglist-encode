package com.bs.lucene.codec.TestCodec;


import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;
import org.apache.lucene.codecs.PostingsFormat;



public class TestCodec extends FilterCodec {
    private final PostingsFormat postings = new SimpleTextPostingsFormat();
    public  TestCodec() {
        super("TestCodec", new Lucene80Codec());
    }

    @Override
    public PostingsFormat postingsFormat() {
        return postings;
    }
}



