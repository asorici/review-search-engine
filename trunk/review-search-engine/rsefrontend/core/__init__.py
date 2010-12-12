from lucene import \
    IndexSearcher, StandardAnalyzer, SimpleFSDirectory, File, \
    VERSION, initVM, Version

from rsefrontend import jcc_jvm

jcc_jvm.attachCurrentThread()
SEARCH_ROOT = "/home/alex/eclipse-workspace/review-search-engine"
INDEX_DIR = SEARCH_ROOT + "/index"


# initialize index searcher structure
directory = SimpleFSDirectory(File(INDEX_DIR))
searcher = IndexSearcher(directory, True)