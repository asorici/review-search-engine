from django.conf.urls.defaults import *
import settings

# Uncomment the next two lines to enable the admin:
from core.views import * 
from django.contrib import admin
from django.views.generic.simple import direct_to_template
admin.autodiscover()

urlpatterns = patterns('',
    # Example:
    # (r'^rsefrontend/', include('rsefrontend.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # (r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    (r'^admin/', include(admin.site.urls)),
    (r'^$', home, {}, 'home'),
    (r'^search/', search, {}, 'search'),
    
    # serving static files
    (r'^rse_media/(?P<path>.*)$', 'django.views.static.serve', { 'document_root': settings.MEDIA_ROOT }),
    (r'^product_feature_results/', direct_to_template, {'template': 'search_by_product_and_feature_results.html'}),
)
