import simplejson
from django.shortcuts import render_to_response, HttpResponse, HttpResponseRedirect

from lucene import \
    QueryParser, IndexSearcher, StandardAnalyzer, SimpleFSDirectory, File, \
    VERSION, initVM, Version

from rsefrontend import jcc_jvm
from rsefrontend.core import searcher

def doSearch(query):
    #### lucene stuff ####
    # attach current thread to jvm
    jcc_jvm.attachCurrentThread()
    
    feature_query, product_query = extractFeatureQueryWords(query)
    print "title:" + product_query + " AND " + "features:" + feature_query
    
    search_query = ""
    if product_query and not feature_query:
        search_query = "title:" + product_query
        return doProductSearch(search_query)
    elif feature_query and not product_query:
        search_query = "features:" + feature_query
        return doFeatureSearch(search_query)
    elif product_query and feature_query:
        search_query = "title:" + product_query + " AND " + "features:" + feature_query
        return doProductFeatureSearch(search_query)
    
    if not search_query:       
        return HttpResponseRedirect("/")

    
def doProductSearch(query):
    scoreDocs = getResultScoreDocs(query)
    
    #### create return structure ####
    results = []
    
    for scoreDoc in scoreDocs:
        doc = searcher.doc(scoreDoc.doc)
        featureMap = simplejson.loads(doc.get("feature-contents"))
        
        positiveFeatureComments = {}
        negativeFeatureComments = {}
        for feat in featureMap["featureMap"].keys():
            #print feat
            positiveFeatureComments[feat] = []
            negativeFeatureComments[feat] = []
            
            for fmd in featureMap["featureMap"][feat]:
                if fmd["connotation"] == True:
                    #positiveFeatureComments[feat] = featureMap["featureMap"][feat]
                    positiveFeatureComments[feat].append(fmd)
                else:
                    #negativeFeatureComments[feat] = featureMap["featureMap"][feat]
                    negativeFeatureComments[feat].append(fmd)
            
            if not positiveFeatureComments[feat]:
                del positiveFeatureComments[feat]
                
            if not negativeFeatureComments[feat]:
                del negativeFeatureComments[feat]
        
        resentry = {"title": doc.get("title"), "content": doc.get("summary"), 
                    "positiveComments": positiveFeatureComments, "negativeComments": negativeFeatureComments }
        results.append(resentry)
    
    return render_to_response("results.html", {"submitted_query": query, "results" : results})
    
def doFeatureSearch(query):
    scoreDocs = getResultScoreDocs(query)
    
    #### create return structure ####
    results = []
    
    for scoreDoc in scoreDocs:
        doc = searcher.doc(scoreDoc.doc)
        featureMap = simplejson.loads(doc.get("feature-contents"))
        
        positiveFeatureComments = {}
        negativeFeatureComments = {}
        for feat in featureMap["featureMap"].keys():
            #print feat
            positiveFeatureComments[feat] = []
            negativeFeatureComments[feat] = []
            
            for fmd in featureMap["featureMap"][feat]:
                if fmd["connotation"] == True:
                    #positiveFeatureComments[feat] = featureMap["featureMap"][feat]
                    positiveFeatureComments[feat].append(fmd)
                else:
                    #negativeFeatureComments[feat] = featureMap["featureMap"][feat]
                    negativeFeatureComments[feat].append(fmd)
            
            if not positiveFeatureComments[feat]:
                del positiveFeatureComments[feat]
                
            if not negativeFeatureComments[feat]:
                del negativeFeatureComments[feat]
                
        resentry = {"title": doc.get("title"), "content": doc.get("summary"), 
                    "positiveComments": positiveFeatureComments, "negativeComments": negativeFeatureComments }
        results.append(resentry)
    
    return render_to_response("results.html", {"submitted_query": query, "results" : results})
    

def doProductFeatureSearch(query):
    
    scoreDocs = getResultScoreDocs(query)
    
    #### create return structure ####
    results = []
    
    for scoreDoc in scoreDocs:
        doc = searcher.doc(scoreDoc.doc)
        featureMap = simplejson.loads(doc.get("feature-contents"))
        
        positiveFeatureComments = {}
        negativeFeatureComments = {}
        for feat in featureMap["featureMap"].keys():
            #print feat
            positiveFeatureComments[feat] = []
            negativeFeatureComments[feat] = []
            
            for fmd in featureMap["featureMap"][feat]:
                if fmd["connotation"] == True:
                    #positiveFeatureComments[feat] = featureMap["featureMap"][feat]
                    positiveFeatureComments[feat].append(fmd)
                else:
                    #negativeFeatureComments[feat] = featureMap["featureMap"][feat]
                    negativeFeatureComments[feat].append(fmd)
            
            if not positiveFeatureComments[feat]:
                del positiveFeatureComments[feat]
                
            if not negativeFeatureComments[feat]:
                del negativeFeatureComments[feat]
        
        resentry = {"title": doc.get("title"), "content": doc.get("summary"), 
                    "positiveComments": positiveFeatureComments, "negativeComments": negativeFeatureComments }
        results.append(resentry)
    
    return render_to_response("results.html", {"submitted_query": query, "results" : results})
    
    
def advancedFeatureSearch(query, sought_feature):
    formatted_query = parser.parse(query)
    scoreDocs = searcher.search(formatted_query, 50).scoreDocs
        
    #### create return structure ####
    results = []
    comments = []
        
    for scoreDoc in scoreDocs:
        doc = searcher.doc(scoreDoc.doc)
        featureMap = simplejson.loads(doc.get("feature-contents"))
        
        comments.append(featureMap["featureMap"][sought_feat]["sentence"])
        resentry = {"feature": sought_feature, "title": doc.get("title"), "review_date": doc.get("modified"),  
                    "comments": comments }
        results.append(resentry)
            
    return render_to_response("search_by_product_and_feature_results.html", {"submitted_query": query, "results" : results})

def getResultScoreDocs(query):
    # create analyzer
    analyzer = StandardAnalyzer(Version.LUCENE_CURRENT)
    
    # create parser for user submitted query
    parser = QueryParser(Version.LUCENE_CURRENT, "title", analyzer)
    parser.setDefaultOperator(QueryParser.Operator.AND)
    formatted_query = parser.parse(query)
    scoreDocs = searcher.search(formatted_query, 50).scoreDocs
    
    return scoreDocs

def extractFeatureQueryWords(query):
    import string
    from lucene import Document, TermQuery, Term
    
    # create analyzer
    aux_analyzer = StandardAnalyzer(Version.LUCENE_CURRENT)
    
    try:
        file = open('../features.txt', 'r')
        
        featurelist = []
        for line in file.readlines():
            words_in_line = line.split()
            featurelist += words_in_line
             
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
            featureQuery = "("
            for i in range(len(featureQueryList)):
                if i == len(featureQueryList) - 1:
                    featureQuery += featureQueryList[i] + ")"
                else:
                    featureQuery += featureQueryList[i] + " AND "
                
            print featureQuery
        
        productQuery = ""
        if productQueryList:
            productQuery = "("
            for i in range(len(productQueryList)):
                if i == len(productQueryList) - 1:
                    productQuery += productQueryList[i] + ")"
                else:
                    productQuery += productQueryList[i] + " AND "
            
        return (featureQuery, productQuery)
    except Exception, ex:
        print "Could not separate feature query words. Reason: ", ex
        return ("", "(" + query + ")")