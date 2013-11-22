#!/usr/bin/env python
 
import threading
import time
import urllib
import urllib2
import requests
from datetime import datetime as dt
import commands
td = dt.now()
ts = td.strftime('%Y-%m-%d_%H-%M-%S')

url = 'http://www.vita-factory.com/api/inws'
 
def ws_send():
        wk_x = commands.getoutput('usbrh')
        ary = wk_x.split(' ')
        params = {'id':'A1','celsius':float(ary[0]),'humidity':float(ary[1]),'d_t':ts}
        params = urllib.urlencode(params)
        print "[%s] !!" % threading.currentThread().getName()
        r = requests.get(url+'?'+params)
        print r.text
        t=threading.Timer(10,ws_send)
        t.start()
 
if __name__ == "__main__":
    t = threading.Thread(target=ws_send)
    t.start()
