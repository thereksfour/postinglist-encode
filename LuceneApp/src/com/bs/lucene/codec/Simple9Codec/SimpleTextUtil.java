package com.bs.lucene.codec.Simple9Codec;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.StringHelper;

import java.io.IOException;
import java.util.Locale;

class SimpleTextUtil {
    public static final byte NEWLINE = 10;
    public static final byte ESCAPE = 92;
    static final BytesRef CHECKSUM = new BytesRef("checksum\t");

    SimpleTextUtil() {
    }

    public static void write(DataOutput out, String s, BytesRefBuilder scratch) throws IOException {
        scratch.copyChars(s, 0, s.length());
        write(out, scratch.get());
    }

    public static void write(DataOutput out, BytesRef b) throws IOException {
        for(int i = 0; i < b.length; ++i) {
            byte bx = b.bytes[b.offset + i];
            if (bx == 10 || bx == 92) {
                out.writeByte((byte)92);
            }

            out.writeByte(bx);
        }

    }

    public static void writeNewline(DataOutput out) throws IOException {
        out.writeByte((byte)10);
    }

    public static void writeSpace(DataOutput out) throws IOException {
        out.writeByte((byte)32);
    }

    public static void readLine(DataInput in, BytesRefBuilder scratch) throws IOException {
        int upto = 0;
        while(true) {
            byte b = in.readByte();
            scratch.grow(1 + upto);
            if (b == 92) {
                scratch.setByteAt(upto++, in.readByte());
            } else {
                if (b == 10) {
                    scratch.setLength(upto);
                    return;
                }
                scratch.setByteAt(upto++, b);
            }
        }
    }
    public static byte[] insertArray(byte[] arr, byte value){
        byte[] res=new byte[arr.length+1];
        int index=0;
        for(int i=0;i<arr.length;i++){
            res[index]=arr[i];
            index++;
        }
        res[res.length-1]=value;
        return res;
    }
    public static void writeChecksum(IndexOutput out, BytesRefBuilder scratch) throws IOException {
        String checksum = String.format(Locale.ROOT, "%020d", out.getChecksum());
        write(out, CHECKSUM);
        write(out, checksum, scratch);
        writeNewline(out);
    }

    public static void checkFooter(ChecksumIndexInput input) throws IOException {
        BytesRefBuilder scratch = new BytesRefBuilder();
        String expectedChecksum = String.format(Locale.ROOT, "%020d", input.getChecksum());
        readLine(input, scratch);
        if (!StringHelper.startsWith(scratch.get(), CHECKSUM)) {
            throw new CorruptIndexException("SimpleText failure: expected checksum line but got " + scratch.get().utf8ToString(), input);
        } else {
            String actualChecksum = (new BytesRef(scratch.bytes(), CHECKSUM.length, scratch.length() - CHECKSUM.length)).utf8ToString();
            if (!expectedChecksum.equals(actualChecksum)) {
                throw new CorruptIndexException("SimpleText checksum failure: " + actualChecksum + " != " + expectedChecksum, input);
            } else if (input.length() != input.getFilePointer()) {
                throw new CorruptIndexException("Unexpected stuff at the end of file, please be careful with your text editor!", input);
            }
        }
    }

    public static BytesRef fromBytesRefString(String s) {
        if (s.length() < 2) {
            throw new IllegalArgumentException("string " + s + " was not created from BytesRef.toString?");
        } else if (s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') {
            String[] parts = s.substring(1, s.length() - 1).split(" ");
            byte[] bytes = new byte[parts.length];

            for(int i = 0; i < parts.length; ++i) {
                bytes[i] = (byte)Integer.parseInt(parts[i], 16);
            }

            return new BytesRef(bytes);
        } else {
            throw new IllegalArgumentException("string " + s + " was not created from BytesRef.toString?");
        }
    }
}

