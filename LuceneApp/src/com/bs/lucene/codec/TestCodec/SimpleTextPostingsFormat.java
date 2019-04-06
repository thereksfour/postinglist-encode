//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bs.lucene.codec.TestCodec;

import java.io.IOException;
import org.apache.lucene.codecs.FieldsConsumer;
import org.apache.lucene.codecs.FieldsProducer;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;

final class SimpleTextPostingsFormat extends PostingsFormat {
    static final String POSTINGS_EXTENSION = "pst";

    public SimpleTextPostingsFormat() {
        super("SimpleText");
    }

    public FieldsConsumer fieldsConsumer(SegmentWriteState state) throws IOException {
        return new SimpleTextFieldsWriter(state);
    }

    public FieldsProducer fieldsProducer(SegmentReadState state) throws IOException {
        return new SimpleTextFieldsReader(state);
    }

    static String getPostingsFileName(String segment, String segmentSuffix) {
        return IndexFileNames.segmentFileName(segment, segmentSuffix, "pst");
    }
}
