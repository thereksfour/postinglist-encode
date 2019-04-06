package com.bs.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;

public class Searcher {

    IndexSearcher indexSearcher;
    IndexReader indexReader;
    Query query;

    public Searcher(String indexDirectoryPath)
            throws IOException{
        Directory indexDirectory =
                FSDirectory.open(Paths.get(indexDirectoryPath));
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
    }

    public TopDocs search(String searchQuery)
            throws IOException {
        query = new TermQuery(new Term(LuceneConstants.CONTENTS, searchQuery));
        return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
    }

    public Document getDocument(ScoreDoc scoreDoc)
            throws  IOException{
        return indexSearcher.doc(scoreDoc.doc);
    }

    public void close() throws IOException{
        indexReader.close();
    }
}
