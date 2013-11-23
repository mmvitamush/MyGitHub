#!/usr/bin/env python
 
import threading
import time
import urllib
import urllib2
import requests
from datetime import datetime as dt
import commands
import random
import MySQLdb
from boto.dynamodb2.table import Table
from boto.dynamodb2.items import Item
import ConfigParser

config_file = "config.ini"
conf = ConfigParser.SafeConfigParser()
conf.read(config_file)

td = dt.now()
ts = td.strftime('%Y-%m-%d_%H-%M-%S')

url = conf.get("apiurl","inws")
d_host=conf.get("mysql", "host")
d_db=conf.get("mysql", "db")
d_user=conf.get("mysql", "user")
d_passwd=conf.get("mysql", "passwd")
d_port=conf.get("mysql", "port")

con=MySQLdb.connect(host=d_host,
                    db=d_db,
                    user=d_user,
                    passwd=d_passwd,
                    port=3306)
cursor = con.cursor()
 
def ws_send():
        wk_x = commands.getoutput('usbrh')
        ary = wk_x.split(' ')
        set_record(conf.get("line","lineid"),conf.get("line","lineno"),float(ary[0]),float(ary[1]))
        params = {'line':conf.get("line","lineid"),'lineno':conf.get("line","lineno"),'celsius':float(ary[0]),'humidity':float(ary[1]),'d_t':ts}
      #  params = urllib.urlencode(params)
        print "[%s] !!" % threading.currentThread().getName()
      #  r = requests.get(url+'?'+params)
        t=threading.Timer(10,ws_send)
        t.start()

def set_record(lineid,lineno,cels,humd):
        sql = 'INSERT INTO mushrecord (lineid,lineno,t_date,celsius,humidity) VALUES (%d,%d,UNIX_TIMESTAMP(),%.2f,%.2f)' % (lineid,lineno,cels,humd)
        cursor.execute(sql)
 
if __name__ == "__main__":

    t = threading.Thread(target=ws_send)
    t.start()
