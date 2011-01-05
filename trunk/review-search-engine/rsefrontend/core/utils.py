import simplejson

from lucene import \
    QueryParser, IndexSearcher, StandardAnalyzer, SimpleFSDirectory, File, \
    VERSION, initVM, Version

from rsefrontend import jcc_jvm
from rsefrontend.core import searcher

def doSearch(query):
    #### lucene stuff ####
    # attach current thread to jvm
    jcc_jvm.attachCurrentThread()
    
    # create analyzer
    analyzer = StandardAnalyzer(Version.LUCENE_CURRENT)
    
    feature_query, product_query = extractFeatureQueryWords(query)
    print feature_query, product_query
    
    parser = QueryParser(Version.LUCENE_CURRENT, "title", analyzer)
    print "title:" + product_query + " AND " + "features:" + feature_query
    
    search_query = ""
    if product_query and not feature_query:
        search_query = "title:" + product_query
    elif feature_query and not product_query:
        search_query = "features:" + feature_query
    elif product_query and feature_query:
        search_query = "title:" + product_query + " AND " + "features:" + feature_query
        
    
    if search_query:    
        formatted_query = parser.parse(search_query)
        scoreDocs = searcher.search(formatted_query, 50).scoreDocs
        
        #### create return structure ####
        results = []
        
        for scoreDoc in scoreDocs:
            doc = searcher.doc(scoreDoc.doc)
            featureMap = simplejson.loads(doc.get("feature-contents"))
            
            positiveFeatureComments = {}
            negativeFeatureComments = {}
            for feat in featureMap["featureMap"].keys():
                #print feat
                if featureMap["featureMap"][feat]["connotation"] == True:
                    positiveFeatureComments[feat] = featureMap["featureMap"][feat]
                else:
                    negativeFeatureComments[feat] = featureMap["featureMap"][feat]
            
            resentry = { "title": doc.get("title"), "content": doc.get("summary"), 
                        "positiveComments": positiveFeatureComments, "negativeComments": negativeFeatureComments }
            results.append(resentry)
            
        return results
    else:
        return []

    
def standard_search(query):
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

def extractFeatureQueryWords(query):
    import string
    from lucene import Document, TermQuery, Term
    
    # create analyzer
    aux_analyzer = StandardAnalyzer(Version.LUCENE_CURRENT)
    
    try:
        file = open('/home/alex/eclipse-workspace/review-search-engine/features.txt', 'r')
        featurelist = map(lambda x: string.strip(x), file.readlines())
        querywordlist = query.split()
        
        featureQueryList = []
        productQueryList = []
        
        for word in querywordlist:
            if word in featurelist:
                featureQueryList.append(word)
            else:
                # create parser for word
                aux_parser = QueryParser(Version.LUCENE_CURRENT, "title", aux_analyzer)
                aux_query = aux_parser.parse(word)
                scoreDocs = searcher.search(aux_query, 50).scoreDocs
                if scoreDocs:
                    productQueryList.append(word)
        
        featureQuery = ""
        if featureQueryList:
            featureQuery = "(" + featureQueryList.pop(0)
            featureQuery = reduce(lambda x: " AND " + x, featureQueryList, featureQuery)
            featureQuery += ")"
        
        productQuery = ""
        if productQueryList:
            productQuery = "(" + productQueryList.pop(0)
            productQuery = reduce(lambda x: " AND " + x, productQueryList, productQuery)
            productQuery += ")"
            
        return (featureQuery, productQuery)
    except Exception, ex:
        print "Could not separate feature query words. Reason: ", ex
        return ("", "(" + query + ")")