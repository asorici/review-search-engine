from lucene import \
    QueryParser, IndexSearcher, StandardAnalyzer, SimpleFSDirectory, File, \
    VERSION, initVM, Version

from rsefrontend import jcc_jvm
from rsefrontend.core import searcher

def standard_search(query):
    #### lucene stuff ####
    # attach current thread to jvm
    jcc_jvm.attachCurrentThread()
    
    # create analyzer
    analyzer = StandardAnalyzer(Version.LUCENE_CURRENT)
    
    # create parser for user submitted query
    parser = QueryParser(Version.LUCENE_CURRENT, "title", analyzer)
    parser.setDefaultOperator(QueryParser.Operator.AND)
    formatted_query = parser.parse(query)
    scoreDocs = searcher.search(formatted_query, 50).scoreDocs
    
    #### create return structure ####
    results = []
    
    
    for scoreDoc in scoreDocs:
        doc = searcher.doc(scoreDoc.doc)
        print doc
        resentry = { "title": doc.get("title") , "content": doc.get("summary") }
        results.append(resentry)
        
    return results