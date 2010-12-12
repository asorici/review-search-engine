from django.shortcuts import render_to_response, get_object_or_404, HttpResponse, HttpResponseRedirect

def home(request):
    if request.method == "GET":
        return render_to_response("index.html")

def search(request):
    if request.method == "POST":
        try:
            query = request.POST['query']
            return HttpResponse("Submitted query: " + query)
        except:
            pass
    else:
        return HttpResponseRedirect("/")
        