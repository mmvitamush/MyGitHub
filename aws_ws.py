#!/usr/bin/env python
 
import threading
import time
import urllib
import urllib2
import requests
from datetime import datetime as dt
import commands
import random
import MySQLdbv
from boto.dynamodb2.table import Table
from boto.dynamodb2.items import Item
import ConfigParser

config_file = "config.ini"
conf = ConfigParser.SafeConfigParser()
conf.read(config_file)

td = dt.now()
ts = td.strftime('%Y-%m-%d_%H-%M-%S')

url = conf.get("apiurl","inws")
 
def ws_send():
        params = {'line':1,'lineno':1,'celsius':random.randint(15,35),'humidity':random.randint(30,80),'d_t':ts}
        params = urllib.urlencode(params)
        print "[%s] !!" % threading.currentThread().getName()
        r = requests.get(url+'?'+params)
        t=threading.Timer(10,ws_send)
        t.start()

def mysql_db_cursor():
    con = MySQLdb.connect(host=conf.get("mysql", "host"),
                          db=conf.get("mysql", "db"),
                          user=conf.get("mysql", "user"),
                          passwd=conf.get("mysql", "passwd"),
                          port=conf.get("mysql", "port")) 
    cursor = con.cursor()
    return cursor
 
if __name__ == "__main__":
    t = threading.Thread(target=ws_send)
    t.start()
