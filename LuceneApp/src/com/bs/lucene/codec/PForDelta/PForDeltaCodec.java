package com.bs.lucene.codec.PForDelta;


import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;


public class PForDeltaCodec extends FilterCodec {
    private final PostingsFormat postings = new SimpleTextPostingsFormat();
    public  PForDeltaCodec() {
        super("PForDeltaCodec", new Lucene80Codec());
    }

    @Override
    public PostingsFormat postingsFormat() {
        return postings;
    }
}



