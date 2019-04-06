package com.bs.lucene;


import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.*;


import com.bs.lucene.codec.PForDelta.PForDeltaCodec;
import com.bs.lucene.codec.Simple9Codec.Simple9Codec;
import com.bs.lucene.codec.TestCodec.TestCodec;
import com.bs.lucene.codec.DGapCodec.DGapCodec;
import com.bs.lucene.codec.VByteCodec.VByteCodec;
import org.apache.commons.io.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Indexer {

    private IndexWriter writer;

    public Indexer(String indexDirectoryPath) throws IOException{
        //this directory will contain the indexes
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setCodec(new PForDeltaCodec());
        config.setUseCompoundFile(false);
        System.out.println(config.getIndexDeletionPolicy());

        //create the indexer
        writer = new IndexWriter(indexDirectory,config);
    }

    public void close() throws IOException{
        writer.close();
    }

    private Document getDocument(File file) throws IOException{
        Document document = new Document();

        //index file contents
        Field contentField = new TextField(LuceneConstants.CONTENTS,
                FileUtils.readFileToString(file,"utf-8"),
                Field.Store.YES);
        //index file name
        Field fileNameField = new StoredField(LuceneConstants.FILE_NAME,
                file.getName());

        //index file path
        Field filePathField = new StoredField(LuceneConstants.FILE_PATH,
                file.getCanonicalPath());

        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);

        return document;
    }

    private void indexFile(File file) throws IOException{
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws IOException{
        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();

        for (File file : files) {
            if(!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                //&& filter.accept(file)
            ){
                indexFile(file);
            }
        }
        return writer.numRamDocs();
    }
}