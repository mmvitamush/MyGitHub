#!/usr/bin/env python
 
import threading
import time
import urllib
import urllib2
import requests

url = 'http://192.168.56.2:5000/api/inws'
params = {'id':'A1','data':33}
params = urllib.urlencode(params)
print params

class test(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.setDaemon(True)
 
    def run(self):
        print "Start."
        while True:
            time.sleep(10)
            r = requests.get(url+'?'+params)
            print r.text
            print "exit"
 
if __name__ == "__main__":
    t = test()
    t.start()
    time.sleep(30)
