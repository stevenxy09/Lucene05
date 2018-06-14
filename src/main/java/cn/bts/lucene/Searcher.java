package cn.bts.lucene;

import java.io.StringReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
* @author stevenxy E-mail:random_xy@163.com
* @Date 2018年6月14日
* @Description 查询
*/
public class Searcher {
	
	public static void search(String indexDir,String q)throws Exception {
		Directory directory=FSDirectory.open(Paths.get(indexDir));
		IndexReader reader=DirectoryReader.open(directory);
		IndexSearcher is=new IndexSearcher(reader);
		//Analyzer analyzer=new StandardAnalyzer();
		/**
		 * 中文解析
		 */
		SmartChineseAnalyzer analyzer=new SmartChineseAnalyzer();
		QueryParser parser=new QueryParser("desc", analyzer);
		Query query=parser.parse(q);
		long start=System.currentTimeMillis();
		TopDocs hits=is.search(query, 10);
		long end=System.currentTimeMillis();
		System.out.println("匹配:"+q+",总共花费:"+(end-start)+"毫秒"+hits.totalHits+"个记录");
		/**
		 * 高亮显示
		 */
		QueryScorer scorer=new QueryScorer(query); //显示得分高的片段(摘要)
		Fragmenter fragmenter=new SimpleSpanFragmenter(scorer);
		SimpleHTMLFormatter simpleHTMLFormatter=new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		Highlighter highlighter=new Highlighter(simpleHTMLFormatter,scorer);
		highlighter.setTextFragmenter(fragmenter);
		for(ScoreDoc scoreDoc:hits.scoreDocs) {
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get("city"));
			System.out.println(doc.get("desc"));
			String desc=doc.get("desc");
			if(desc!=null) {
				/*把权重高的显示出来*/
				TokenStream tokenStream=analyzer.tokenStream("desc", new StringReader(desc));
				String str=highlighter.getBestFragment(tokenStream, desc);
				System.out.println(str);
			}
		}
		reader.close();
	}
	
	public static void main(String[] args) {
		String indexDir="D:\\lucene6";
		String q="南京文化";
		try {
			search(indexDir, q);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
