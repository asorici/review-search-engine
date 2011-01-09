from django.shortcuts import render_to_response, get_object_or_404, HttpResponse, HttpResponseRedirect
from utils import standard_search, doSearch

def home(request):
    if request.method == "GET":
        return render_to_response("index.html")

def search(request):
    if request.method == "POST":
        try:
            query = request.POST['query']
            #results = standard_search(query)
            #results = doSearch(query)
            #return render_to_response("results.html", {"submitted_query": query, "results" : results})
            doSearch(query)
        except Exception, ex:
            print ex
            return HttpResponseRedirect("/")
    else:
        return HttpResponseRedirect("/")
        