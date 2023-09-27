/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.media.search;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;

public class SearchService {

	private final Directory index;

	private final Analyzer analyzer;

	private AnalyzingInfixSuggester suggester;

	private IndexReader reader;


	public SearchService() {
		index = new ByteBuffersDirectory();
		analyzer = new GermanAnalyzer();
	}

	public CompletableFuture<Void> createIndex(Document document) {
		return CompletableFuture.runAsync(() -> {
			IndexWriterConfig config = new IndexWriterConfig(analyzer);

			try (IndexWriter writer = new IndexWriter(index, config)) {
				for (Page page : document.getPages()) {
					org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
					doc.add(new StringField("title", document.getName(), Field.Store.YES));
					doc.add(new StoredField("number", page.getPageNumber()));
					doc.add(new TextField("content", page.getPageText(), Field.Store.YES));

					writer.addDocument(doc);
				}
			}
			catch (IOException e) {
				throw new CompletionException(e);
			}

			try {
				initReader();
				initSuggester();
			}
			catch (IOException e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> destroyIndex(Document document) {
		return CompletableFuture.runAsync(() -> {
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			Term term = new Term("title", document.getName());

			try (IndexWriter writer = new IndexWriter(index, config)) {
				writer.deleteDocuments(term);
			}
			catch (IOException e) {
				throw new CompletionException(e);
			}

			try {
				initReader();
				destroySuggester();
			}
			catch (IOException e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<SearchResult> searchIndex(String queryString) {
		return CompletableFuture.supplyAsync(() -> {
			List<Integer> pageIndices = new ArrayList<>();
			List<String> suggestions = new ArrayList<>();

			try {
				Query query = new QueryParser("content", analyzer).parse(queryString);
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs topDocs = searcher.search(query, 10, Sort.INDEXORDER);

				for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
					org.apache.lucene.document.Document doc = searcher.doc(scoreDoc.doc);

					pageIndices.add(doc.getField("number").numericValue().intValue());
				}

				List<Lookup.LookupResult> suggestList = suggester.lookup(queryString, true, 5);

				for (Lookup.LookupResult lookupResult : suggestList) {
					suggestions.add(lookupResult.key.toString());
				}
			}
			catch (IOException | ParseException e) {
				throw new CompletionException(e);
			}

			return new SearchResult(queryString, pageIndices, suggestions);
		});
	}

	private void initReader() throws IOException {
		if (nonNull(reader)) {
			reader.close();
		}

		reader = DirectoryReader.open(index);
	}

	private void initSuggester() throws IOException {
		if (isNull(suggester)) {
			suggester = new AnalyzingInfixSuggester(new ByteBuffersDirectory(), analyzer);
		}

		suggester.build(new LuceneDictionary(reader, "content"));
	}

	private void destroySuggester() throws IOException {
		if (nonNull(suggester)) {
			try {
				suggester.close();
			}
			finally {
				suggester = null;
			}
		}
	}
}
