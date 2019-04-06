//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bs.lucene.codec.Simple9Codec;

import com.bs.lucene.codec.algorithm.DGap;
import org.apache.lucene.codecs.FieldsProducer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.BufferedChecksumIndexInput;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.*;
import org.apache.lucene.util.fst.*;
import org.apache.lucene.util.fst.BytesRefFSTEnum.InputOutput;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.PairOutputs.Pair;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

class SimpleTextFieldsReader extends FieldsProducer {
    private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(SimpleTextFieldsReader.class) + RamUsageEstimator.shallowSizeOfInstance(TreeMap.class);
    private final TreeMap<String, Long> fields;
    private final IndexInput in;
    private final FieldInfos fieldInfos;
    private final int maxDoc;
    private static final long TERMS_BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(SimpleTextFieldsReader.SimpleTextTerms.class) + RamUsageEstimator.shallowSizeOfInstance(BytesRef.class) + RamUsageEstimator.shallowSizeOfInstance(CharsRef.class);
    private final Map<String, SimpleTextFieldsReader.SimpleTextTerms> termsCache = new HashMap();

    public SimpleTextFieldsReader(SegmentReadState state) throws IOException {
        this.maxDoc = state.segmentInfo.maxDoc();
        this.fieldInfos = state.fieldInfos;
        this.in = state.directory.openInput(SimpleTextPostingsFormat.getPostingsFileName(state.segmentInfo.name, state.segmentSuffix), state.context);
        boolean success = false;

        try {
            this.fields = this.readFields(this.in.clone());
            success = true;
        } finally {
            if (!success) {
                IOUtils.closeWhileHandlingException(new Closeable[]{this});
            }

        }

    }

    private TreeMap<String, Long> readFields(IndexInput in) throws IOException {
        ChecksumIndexInput input = new BufferedChecksumIndexInput(in);
        BytesRefBuilder scratch = new BytesRefBuilder();
        TreeMap fields = new TreeMap();

        while(true) {
            SimpleTextUtil.readLine(input, scratch);
            if (scratch.get().equals(SimpleTextFieldsWriter.END)) {
                SimpleTextUtil.checkFooter(input);
                return fields;
            }

            if (StringHelper.startsWith(scratch.get(), SimpleTextFieldsWriter.FIELD)) {
                String fieldName = new String(scratch.bytes(), SimpleTextFieldsWriter.FIELD.length, scratch.length() - SimpleTextFieldsWriter.FIELD.length, StandardCharsets.UTF_8);
                fields.put(fieldName, input.getFilePointer());
            }
        }
    }

    public Iterator<String> iterator() {
        return Collections.unmodifiableSet(this.fields.keySet()).iterator();
    }

    public synchronized Terms terms(String field) throws IOException {
        SimpleTextFieldsReader.SimpleTextTerms terms = (SimpleTextFieldsReader.SimpleTextTerms)this.termsCache.get(field);
        if (terms == null) {
            Long fp = (Long)this.fields.get(field);
            if (fp == null) {
                return null;
            }

            terms = new SimpleTextFieldsReader.SimpleTextTerms(field, fp, this.maxDoc);
            this.termsCache.put(field, terms);
        }

        return terms;
    }

    public int size() {
        return -1;
    }

    public void close() throws IOException {
        this.in.close();
    }

    public synchronized long ramBytesUsed() {
        long sizeInBytes = BASE_RAM_BYTES_USED + (long)(this.fields.size() * 2 * RamUsageEstimator.NUM_BYTES_OBJECT_REF);

        SimpleTextFieldsReader.SimpleTextTerms simpleTextTerms;
        for(Iterator var3 = this.termsCache.values().iterator(); var3.hasNext(); sizeInBytes += simpleTextTerms != null ? simpleTextTerms.ramBytesUsed() : 0L) {
            simpleTextTerms = (SimpleTextFieldsReader.SimpleTextTerms)var3.next();
        }

        return sizeInBytes;
    }

    public synchronized Collection<Accountable> getChildResources() {
        return Accountables.namedAccountables("field", this.termsCache);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "(fields=" + this.fields.size() + ")";
    }

    public void checkIntegrity() throws IOException {
    }

    private class SimpleTextTerms extends Terms implements Accountable {
        private final long termsStart;
        private final FieldInfo fieldInfo;
        private final int maxDoc;
        private long sumTotalTermFreq;
        private long sumDocFreq;
        private int docCount;
        private FST<Pair<Long, Pair<Long, Long>>> fst;
        private int termCount;
        private final BytesRefBuilder scratch = new BytesRefBuilder();
        private final BytesRefBuilder scratchUTF16 = new BytesRefBuilder();

        public SimpleTextTerms(String field, long termsStart, int maxDoc) throws IOException {
            this.maxDoc = maxDoc;
            this.termsStart = termsStart;
            this.fieldInfo = SimpleTextFieldsReader.this.fieldInfos.fieldInfo(field);
            this.loadTerms();
        }

        private void loadTerms() throws IOException {
            PositiveIntOutputs posIntOutputs = PositiveIntOutputs.getSingleton();
            PairOutputs<Long, Long> outputsInner = new PairOutputs(posIntOutputs, posIntOutputs);
            PairOutputs<Long, Pair<Long, Long>> outputs = new PairOutputs(posIntOutputs, outputsInner);
            Builder<Pair<Long, Pair<Long, Long>>> b = new Builder(INPUT_TYPE.BYTE1, outputs);
            IndexInput in = SimpleTextFieldsReader.this.in.clone();
            in.seek(this.termsStart);
            BytesRefBuilder lastTerm = new BytesRefBuilder();
            long lastDocsStart = -1L;
            int docFreq = 0;
            long totalTermFreq = 0L;
            FixedBitSet visitedDocs = new FixedBitSet(this.maxDoc);
            IntsRefBuilder scratchIntsRef = new IntsRefBuilder();

            while(true) {
                lastDocsStart = in.getFilePointer();
                SimpleTextUtil.readLine(in, this.scratch);
                if (this.scratch.get().equals(SimpleTextFieldsWriter.END) || StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.FIELD)) {
                    if (lastDocsStart != -1L) {
                        this.sumTotalTermFreq += totalTermFreq;
                    }
                    this.docCount = visitedDocs.cardinality();
                    this.fst = b.finish();
                    return;
                }


                byte[] Tmp = this.scratch.bytes();
                int len;
                int scratchLen = this.scratch.length();
                for(int i=0;i<scratchLen;i++) {
                    if (Tmp[i] == 0x20){
                        lastTerm.grow(i);
                        System.arraycopy(this.scratch.bytes(), 0, lastTerm.bytes(), 0, i);
                        lastTerm.setLength(i);
                        ++this.termCount;
                        //System.out.println("Term: "+Arrays.toString(lastTerm.bytes()));
                        //System.out.println("Scratch: "+Arrays.toString(this.scratch.bytes()));
                        //System.out.println("ScratchLen: "+scratchLen);
                        int[] intArr = {};
                        for(int j=i;j<scratchLen-3;j+=4) {
                            ++docFreq;
                            ++this.sumDocFreq;
                            ++totalTermFreq;
                            this.scratchUTF16.copyBytes(this.scratch.bytes(), j+1, 4);
                            len = byteArrayToInt(this.scratchUTF16.bytes());
                            //System.out.println("Int: "+len);
                            intArr = insertArray(intArr,len);
                        }
                        intArr = SimpleTextFieldsWriter.MyS9.Simple9Decode(intArr);
                        //System.out.println("Read: "+Arrays.toString(intArr));
                        for(int j=0;j<intArr.length;j++) {
                            visitedDocs.set(intArr[j]);
                        }
                        break;
                    }
                }
                //System.out.println("Term: "+Arrays.toString(lastTerm.get().bytes));
                //System.out.println("lastDocsStart: "+lastDocsStart);
                //System.out.println("DocFreq: "+docFreq);
                b.add(Util.toIntsRef(lastTerm.get(), scratchIntsRef), outputs.newPair(lastDocsStart, outputsInner.newPair((long)docFreq, totalTermFreq)));
            }
        }
        public int[] insertArray(int[] arr, int value){
            int[] res=new int[arr.length+1];
            int index=0;
            for(int i=0;i<arr.length;i++){
                res[index]=arr[i];
                index++;
            }
            res[res.length-1]=value;
            return res;
        }
        public int byteArrayToInt(byte[] b) {
            return   b[3] & 0xFF |
                    (b[2] & 0xFF) << 8 |
                    (b[1] & 0xFF) << 16 |
                    (b[0] & 0xFF) << 24;
        }

        public long ramBytesUsed() {
            return SimpleTextFieldsReader.TERMS_BASE_RAM_BYTES_USED + (this.fst != null ? this.fst.ramBytesUsed() : 0L) + RamUsageEstimator.sizeOf(this.scratch.bytes()) + RamUsageEstimator.sizeOf(this.scratchUTF16.bytes());
        }

        public Collection<Accountable> getChildResources() {
            return this.fst == null ? Collections.emptyList() : Collections.singletonList(Accountables.namedAccountable("term cache", this.fst));
        }

        public String toString() {
            return this.getClass().getSimpleName() + "(terms=" + this.termCount + ",postings=" + this.sumDocFreq + ",positions=" + this.sumTotalTermFreq + ",docs=" + this.docCount + ")";
        }

        public TermsEnum iterator() throws IOException {
            return (TermsEnum)(this.fst != null ? SimpleTextFieldsReader.this.new SimpleTextTermsEnum(this.fst, this.fieldInfo.getIndexOptions()) : TermsEnum.EMPTY);
        }

        public long size() {
            return (long)this.termCount;
        }

        public long getSumTotalTermFreq() {
            return this.sumTotalTermFreq;
        }

        public long getSumDocFreq() throws IOException {
            return this.sumDocFreq;
        }

        public int getDocCount() throws IOException {
            return this.docCount;
        }

        public boolean hasFreqs() {
            return this.fieldInfo.getIndexOptions().compareTo(IndexOptions.DOCS_AND_FREQS) >= 0;
        }

        public boolean hasOffsets() {
            return this.fieldInfo.getIndexOptions().compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS) >= 0;
        }

        public boolean hasPositions() {
            return this.fieldInfo.getIndexOptions().compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS) >= 0;
        }

        public boolean hasPayloads() {
            return this.fieldInfo.hasPayloads();
        }
    }

    private class SimpleTextPostingsEnum extends PostingsEnum {
        private final IndexInput inStart;
        private final IndexInput in;
        private int docID = -1;
        private int tf;
        private final BytesRefBuilder scratch = new BytesRefBuilder();
        private final BytesRefBuilder scratch2 = new BytesRefBuilder();
        private final CharsRefBuilder scratchUTF16 = new CharsRefBuilder();
        private final CharsRefBuilder scratchUTF16_2 = new CharsRefBuilder();
        private int pos;
        private BytesRef payload;
        private long nextDocStart;
        private boolean readOffsets;
        private boolean readPositions;
        private int startOffset;
        private int endOffset;
        private int cost;

        public SimpleTextPostingsEnum() {
            this.inStart = SimpleTextFieldsReader.this.in;
            this.in = this.inStart.clone();
        }

        public boolean canReuse(IndexInput in) {
            return in == this.inStart;
        }

        public SimpleTextFieldsReader.SimpleTextPostingsEnum reset(long fp, IndexOptions indexOptions, int docFreq) {
            this.nextDocStart = fp;
            this.docID = -1;
            this.readPositions = indexOptions.compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS) >= 0;
            this.readOffsets = indexOptions.compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS) >= 0;
            if (!this.readOffsets) {
                this.startOffset = -1;
                this.endOffset = -1;
            }

            this.cost = docFreq;
            return this;
        }

        public int docID() {
            return this.docID;
        }

        public int freq() throws IOException {
            return this.tf;
        }

        public int nextDoc() throws IOException {
            boolean first = true;
            this.in.seek(this.nextDocStart);
            long posStart = 0L;
            while(true) {
                while(true) {
                    long lineStart = this.in.getFilePointer();
                    SimpleTextUtil.readLine(this.in, this.scratch);
                    byte[] Tmp = this.scratch.bytes();
                    for(int i=0;i<Tmp.length;i++) {
                        if (Tmp[i] == 0x20) {
                            this.scratchUTF16.copyUTF8Bytes(this.scratch.bytes(), 0, i);
                            this.docID = ArrayUtil.parseInt(this.scratchUTF16.chars(), 0, this.scratchUTF16.length());
                            System.out.println(this.docID);
                            System.out.println("DocId: "+this.docID);
                            return this.docID;
                        }
                    }

                    if (StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.DOC)) {
                        if (!first) {
                            this.nextDocStart = lineStart;
                            this.in.seek(posStart);
                            return this.docID;
                        }

                        this.scratchUTF16.copyUTF8Bytes(this.scratch.bytes(), SimpleTextFieldsWriter.DOC.length, this.scratch.length() - SimpleTextFieldsWriter.DOC.length);
                        this.docID = ArrayUtil.parseInt(this.scratchUTF16.chars(), 0, this.scratchUTF16.length());
                        this.tf = 0;
                        first = false;
                    } else if (!StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.POS) && !StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.START_OFFSET) && !StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.END_OFFSET) && !StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.PAYLOAD)) {
                        assert StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.TERM) || StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.FIELD) || StringHelper.startsWith(this.scratch.get(), SimpleTextFieldsWriter.END);

                        if (!first) {
                            this.nextDocStart = lineStart;
                            this.in.seek(posStart);
                            return this.docID;
                        }

                        return this.docID = 2147483647;
                    }
                }
            }
        }

        public int advance(int target) throws IOException {
            return this.slowAdvance(target);
        }

        public int nextPosition() throws IOException {
            return this.pos;
        }

        public int startOffset() throws IOException {
            return this.startOffset;
        }

        public int endOffset() throws IOException {
            return this.endOffset;
        }

        public BytesRef getPayload() {
            return this.payload;
        }

        public long cost() {
            return (long)this.cost;
        }
    }

    private class SimpleTextDocsEnum extends PostingsEnum {
        private final IndexInput inStart;
        private final IndexInput in;
        private boolean omitTF;
        private int docID = -1;
        private int tf;
        private final BytesRefBuilder scratch = new BytesRefBuilder();
        private final BytesRefBuilder scratchUTF16 = new BytesRefBuilder();
        private int cost;
        private int[] DocList = {};
        private int index = 0;
        private boolean First = true;

        public SimpleTextDocsEnum() {
            this.inStart = SimpleTextFieldsReader.this.in;
            this.in = this.inStart.clone();
        }

        public boolean canReuse(IndexInput in) {
            return in == this.inStart;
        }

        public SimpleTextFieldsReader.SimpleTextDocsEnum reset(long fp, boolean omitTF, int docFreq) throws IOException {
            this.in.seek(fp);
            this.omitTF = omitTF;
            this.docID = -1;
            this.tf = 1;
            this.cost = docFreq;
            return this;
        }

        public int docID() {
            return this.docID;
        }

        public int freq() throws IOException {
            //System.out.println("invoke freq 2: "+this.tf);
            return this.tf;
        }

        public int nextPosition() throws IOException {
            return -1;
        }

        public int startOffset() throws IOException {
            return -1;
        }

        public int endOffset() throws IOException {
            return -1;
        }

        public BytesRef getPayload() throws IOException {
            return null;
        }

        public int nextDoc() throws IOException {
            if (this.docID == 2147483647) {
                return this.docID;
            } else if (this.First) {
                SimpleTextUtil.readLine(this.in, this.scratch);
                //System.out.println("Tmp: " + Arrays.toString(this.scratch.bytes()));
                byte[] Tmp = this.scratch.bytes();
                boolean termFlag = true;
                int firstIndex = 0;
                int secondIndex = 0;
                int scratchLen = this.scratch.length();
                for (int i = 0; i < scratchLen; i++) {
                    //System.out.println("Tmp: " + Tmp[i]);
                    if (Tmp[i] == 0x20) {
                        for(int j=i;j<scratchLen-3;j+=4) {
                            this.scratchUTF16.copyBytes(this.scratch.bytes(), j+1, 4);
                            this.docID = byteArrayToInt(this.scratchUTF16.bytes());
                            this.DocList = insertArray(this.DocList, this.docID);
                            this.tf++;
                        }
                        //System.out.println("Before DGap: "+Arrays.toString(this.DocList));
                        this.DocList = SimpleTextFieldsWriter.MyS9.Simple9Decode(this.DocList);
                        //System.out.println("After DGap: "+Arrays.toString(this.DocList));

                        First = false;
                        return this.DocList[this.index++];
                    }
                }
                return this.docID = 2147483647;
            } else if(this.index != this.DocList.length){
                this.docID = this.DocList[this.index++];
                return this.docID;
            } else {
                return this.docID = 2147483647;
            }
        }
        public int byteArrayToInt(byte[] b) {
            return   b[3] & 0xFF |
                    (b[2] & 0xFF) << 8 |
                    (b[1] & 0xFF) << 16 |
                    (b[0] & 0xFF) << 24;
        }
        public int[] insertArray(int[] arr, int value){
            int[] res=new int[arr.length+1];
            int index=0;
            for(int i=0;i<arr.length;i++){
                res[index]=arr[i];
                index++;
            }
            res[res.length-1]=value;
            return res;
        }

        public int advance(int target) throws IOException {
            return this.slowAdvance(target);
        }

        public long cost() {
            return (long)this.cost;
        }
    }

    private class SimpleTextTermsEnum extends BaseTermsEnum {
        private final IndexOptions indexOptions;
        private int docFreq;
        private long totalTermFreq;
        private long docsStart;
        private boolean ended;
        private final BytesRefFSTEnum<Pair<Long, Pair<Long, Long>>> fstEnum;

        public SimpleTextTermsEnum(FST<Pair<Long, Pair<Long, Long>>> fst, IndexOptions indexOptions) {
            this.indexOptions = indexOptions;
            this.fstEnum = new BytesRefFSTEnum(fst);
        }

        public boolean seekExact(BytesRef text) throws IOException {
            InputOutput<Pair<Long, Pair<Long, Long>>> result = this.fstEnum.seekExact(text);
            if (result != null) {
                Pair<Long, Pair<Long, Long>> pair1 = (Pair)result.output;
                Pair<Long, Long> pair2 = (Pair)pair1.output2;
                this.docsStart = (Long)pair1.output1;
                this.docFreq = ((Long)pair2.output1).intValue();
                this.totalTermFreq = (Long)pair2.output2;
                return true;
            } else {
                return false;
            }
        }

        public SeekStatus seekCeil(BytesRef text) throws IOException {
            InputOutput<Pair<Long, Pair<Long, Long>>> result = this.fstEnum.seekCeil(text);
            if (result == null) {
                return SeekStatus.END;
            } else {
                Pair<Long, Pair<Long, Long>> pair1 = (Pair)result.output;
                Pair<Long, Long> pair2 = (Pair)pair1.output2;
                this.docsStart = (Long)pair1.output1;
                this.docFreq = ((Long)pair2.output1).intValue();
                this.totalTermFreq = (Long)pair2.output2;
                return result.input.equals(text) ? SeekStatus.FOUND : SeekStatus.NOT_FOUND;
            }
        }

        public BytesRef next() throws IOException {
            assert !this.ended;

            InputOutput<Pair<Long, Pair<Long, Long>>> result = this.fstEnum.next();
            if (result != null) {
                Pair<Long, Pair<Long, Long>> pair1 = (Pair)result.output;
                Pair<Long, Long> pair2 = (Pair)pair1.output2;
                this.docsStart = (Long)pair1.output1;
                this.docFreq = ((Long)pair2.output1).intValue();
                this.totalTermFreq = (Long)pair2.output2;
                return result.input;
            } else {
                return null;
            }
        }

        public BytesRef term() {
            return this.fstEnum.current().input;
        }

        public long ord() throws IOException {
            throw new UnsupportedOperationException();
        }

        public void seekExact(long ord) {
            throw new UnsupportedOperationException();
        }

        public int docFreq() {

            return this.docFreq;
        }

        public long totalTermFreq() {
            return this.indexOptions == IndexOptions.DOCS ? (long)this.docFreq : this.totalTermFreq;
        }

        public PostingsEnum postings(PostingsEnum reuse, int flags) throws IOException {
            boolean hasPositions = this.indexOptions.compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS) >= 0;
            if (hasPositions && PostingsEnum.featureRequested(flags, (short)24)) {
                SimpleTextFieldsReader.SimpleTextPostingsEnum docsAndPositionsEnum;
                if (reuse != null && reuse instanceof SimpleTextFieldsReader.SimpleTextPostingsEnum && ((SimpleTextFieldsReader.SimpleTextPostingsEnum)reuse).canReuse(SimpleTextFieldsReader.this.in)) {
                    docsAndPositionsEnum = (SimpleTextFieldsReader.SimpleTextPostingsEnum)reuse;
                } else {
                    docsAndPositionsEnum = SimpleTextFieldsReader.this.new SimpleTextPostingsEnum();
                }

                return docsAndPositionsEnum.reset(this.docsStart, this.indexOptions, this.docFreq);
            } else {
                SimpleTextFieldsReader.SimpleTextDocsEnum docsEnum;
                if (reuse != null && reuse instanceof SimpleTextFieldsReader.SimpleTextDocsEnum && ((SimpleTextFieldsReader.SimpleTextDocsEnum)reuse).canReuse(SimpleTextFieldsReader.this.in)) {
                    docsEnum = (SimpleTextFieldsReader.SimpleTextDocsEnum)reuse;
                } else {
                    docsEnum = SimpleTextFieldsReader.this.new SimpleTextDocsEnum();
                }

                return docsEnum.reset(this.docsStart, this.indexOptions == IndexOptions.DOCS, this.docFreq);
            }
        }

        public ImpactsEnum impacts(int flags) throws IOException {
            return new SlowImpactsEnum(this.postings((PostingsEnum)null, flags));
        }
    }
}
