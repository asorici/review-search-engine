from django.shortcuts import render_to_response, get_object_or_404, HttpResponse, HttpResponseRedirect
from utils import standard_search

def home(request):
    if request.method == "GET":
        return render_to_response("index.html")

def search(request):
    if request.method == "POST":
        try:
            query = request.POST['query']
            results = standard_search(query)
            return render_to_response("results.html", {"submitted_query": query, "results" : results})
        except:
            return HttpResponseRedirect("/")
    else:
        return HttpResponseRedirect("/")
        