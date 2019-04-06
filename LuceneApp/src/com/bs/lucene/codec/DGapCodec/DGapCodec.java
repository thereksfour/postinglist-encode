package com.bs.lucene.codec.DGapCodec;


import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;


public class DGapCodec extends FilterCodec {
    private final PostingsFormat postings = new SimpleTextPostingsFormat();
    public  DGapCodec() {
        super("DGapCodec", new Lucene80Codec());
    }

    @Override
    public PostingsFormat postingsFormat() {
        return postings;
    }
}



