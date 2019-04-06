//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bs.lucene.codec.DGapCodec;

import com.bs.lucene.codec.algorithm.DGap;
import org.apache.lucene.codecs.FieldsConsumer;
import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

class SimpleTextFieldsWriter extends FieldsConsumer {
    private IndexOutput out;
    private final BytesRefBuilder scratch = new BytesRefBuilder();
    private final SegmentWriteState writeState;
    final String segment;
    static final BytesRef END = new BytesRef("END");
    static final BytesRef FIELD = new BytesRef("field\t");
    static final BytesRef TERM = new BytesRef("  term ");
    static final BytesRef DOC = new BytesRef("    doc ");
    static final BytesRef FREQ = new BytesRef("      freq ");
    static final BytesRef POS = new BytesRef("      pos ");
    static final BytesRef START_OFFSET = new BytesRef("      startOffset ");
    static final BytesRef END_OFFSET = new BytesRef("      endOffset ");
    static final BytesRef PAYLOAD = new BytesRef("        payload ");

    public SimpleTextFieldsWriter(SegmentWriteState writeState) throws IOException {
        String fileName = SimpleTextPostingsFormat.getPostingsFileName(writeState.segmentInfo.name, writeState.segmentSuffix);
        this.segment = writeState.segmentInfo.name;
        this.out = writeState.directory.createOutput(fileName, writeState.context);
        this.writeState = writeState;
    }

    public void write(Fields fields, NormsProducer norms) throws IOException {
        this.write(this.writeState.fieldInfos, fields);
    }

    public void write(FieldInfos fieldInfos, Fields fields) throws IOException {
        Iterator var3 = fields.iterator();

        while (true) {
            String field;
            Terms terms;
            do {
                if (!var3.hasNext()) {
                    return;
                }

                field = (String) var3.next();
                terms = fields.terms(field);
            } while (terms == null);
            FieldInfo fieldInfo = fieldInfos.fieldInfo(field);
            boolean wroteField = false;
            boolean hasPositions = terms.hasPositions();
            int flags = 0;
            TermsEnum termsEnum = terms.iterator();
            PostingsEnum postingsEnum = null;

            label105:
            while (true) {
                BytesRef term = termsEnum.next();
                if (term == null) {
                    break;
                }

                postingsEnum = termsEnum.postings(postingsEnum);
                assert postingsEnum != null : "termsEnum=" + termsEnum + " hasPos=" + hasPositions + " flags=" + flags;

                boolean wroteTerm = false;
                int[] intArr = {};
                while (true) {
                    int doc = postingsEnum.nextDoc();
                    if (doc == 2147483647) {
                        DGap dGap = new DGap();
                        intArr = dGap.DGapEncode(intArr);
                        //System.out.println("write: "+ Arrays.toString(intArr));
                        this.write(intArr);
                        this.newline();
                        continue label105;
                    }
                    if (!wroteTerm) {
                        if (!wroteField) {
                            this.write(FIELD);
                            this.write(field);
                            this.newline();
                            wroteField = true;
                        }
                        this.write(term);
                        this.space();
                        wroteTerm = true;
                    }
                    intArr = insertArray(intArr,doc);
                }
            }
        }
    }
    public static int[] insertArray(int[] arr, int value){
        int[] res=new int[arr.length+1];
        int index=0;
        for(int i=0;i<arr.length;i++){
            res[index]=arr[i];
            index++;
        }
        res[res.length-1]=value;
        return res;
    }
    private void write(int[] iArr) throws IOException{

        for(int i = 0; i<iArr.length;i++){
            byte[] bt = intToByteArray(iArr[i]);
            BytesRef by = new BytesRef(bt,0,bt.length);
            write(by);
        }
    }
    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
    private void write(String s) throws IOException {
        SimpleTextUtil.write(this.out, s, this.scratch);
    }

    private void write(BytesRef b) throws IOException {
        SimpleTextUtil.write(this.out, b);
    }

    private void newline() throws IOException {
        SimpleTextUtil.writeNewline(this.out);
    }

    private void space() throws IOException {
        SimpleTextUtil.writeSpace(this.out);
    }

    public void close() throws IOException {
        if (this.out != null) {
            try {
                this.write(END);
                this.newline();
                SimpleTextUtil.writeChecksum(this.out, this.scratch);
            } finally {
                this.out.close();
                this.out = null;
            }
        }

    }
}
