#!/usr/bin/env python
# coding:utf-8
import tornado.ioloop  # @UnresolvedImport
#import tornado.httpserver  # @UnresolvedImport
#import tornado.httpclient  # @UnresolvedImport
import tornado.web  # @UnresolvedImport
import tornado.gen  # @UnresolvedImport
import tornado.websocket  # @UnresolvedImport

ws_client = []

from tornado.options import define, options, parse_command_line  # @UnresolvedImport
import json
import os
import logging
import MySQLdb  # @UnresolvedImport


define("port", default=8000, type=int)

class Application(tornado.web.Application):
 
    def __init__(self):
        handlers = [
            (r'/', MainHandler),
            (r'/dashboard',DashBoardHandler),
            (r'/list', ListHandler),
            (r'/chart',ChartHandler),
            (r'/api/getchart',GetChartHandler),
            (r'/api/inws',InWsHandler),
            (r'/auth/login',AuthLoginHandler),
            (r'/auth/logout',AuthLogoutHandler),
            (r'/ws',EchoWebSocket)
         ]
        settings = dict(
            cookie_secret='gaofjawpoer940r34823842398429afadfi4iias',
            static_path=os.path.join(os.path.dirname(__file__), "static"),
            template_path=os.path.join(os.path.dirname(__file__), "templates"),
            xsrf_cookies=False,
            login_url='/auth/login',
            autoescape="xhtml_escape",
            debug=True,
            )
        tornado.web.Application.__init__(self, handlers, **settings)

class BaseHandler(tornado.web.RequestHandler):
    cookie_username = "username"

    def get_current_user(self):
        username = self.get_secure_cookie(self.cookie_username)
        logging.debug('BaseHandler - username: %s' % username)
        if not username: return None
        return tornado.escape.utf8(username)

    def set_current_user(self, username):
        self.set_secure_cookie(self.cookie_username, tornado.escape.utf8(username))

    def clear_current_user(self):
        self.clear_cookie(self.cookie_username)
        
        
class MainHandler(BaseHandler):
    @tornado.web.authenticated
    def get(self):
        self.write("Hello,mushroom")

class AuthLoginHandler(BaseHandler):
    def get(self):
        self.render("login.html", next=self.get_argument("next","/"))

    def post(self):
        username = self.get_argument("username")
        password = self.get_argument("password")
        logging.debug('AuthLoginHandler:post %s %s' % (username, password)) 
        cur = mysql_db_cursor()
        cur.execute("select count(id) from tcpms where username='%s' and passward='%s'" % (username,password))
        res=cur.fetchone()
        cnt=res[0]
        if cnt > 0:
            self.set_current_user(username)
            self.redirect("/dashboard")
        else:
            self.write_error(403)       

class AuthLogoutHandler(BaseHandler):
    def get(self):
        self.clear_current_user()
        self.redirect("/")

class DashBoardHandler(BaseHandler):
    #@tornado.web.authenticated
    def get(self):
        self.render("dashboard.html")
     
class ListHandler(tornado.web.RequestHandler):
    @tornado.web.asynchronous
    def get(self):
        self.render("list.html")

    def post(self):
        self.redirect("/")
    def __handle_response(self, response):
        if response.error:
            response.rethrow()
        self.set_header('content-type', 'application/atom+xml; charset=UTF-8')
        self.write(response.body)
        self.finish()
        
class ChartHandler(tornado.web.RequestHandler): 
        @tornado.web.authenticated          
        def get(self):
            self.render("chart.html")
        
 
 
class GetChartHandler(tornado.web.RequestHandler):       
        @property
        def db(self):
                return self.application.db
        @tornado.web.asynchronous
        def post(self, *args):
            logging.debug('start: %s | end: %s' % (self.get_argument('start'),self.get_argument('end')))
            #col = self.db['ch_data']
            ary = []
            #for data in col.find({u'line':'A1'}).limit(50):
#                 ary.append(json.dumps(data,default=json_util.default))
            self.set_header('Content-Type', 'application/json')
            #self.write(json.JSONEncoder().encode(ary))
            self.finish(json.JSONEncoder().encode(ary))
            
class EchoWebSocket(tornado.websocket.WebSocketHandler):
    def open(self):
        print "WebSocket opened"
        if self not in ws_client:
            ws_client.append(self)
        

    def on_message(self, message):
        self.write_message(u"You said: " + message)

    def on_close(self):
        print "WebSocket closed"
        if self in ws_client:
            ws_client.remove(self)

class InWsHandler(tornado.web.RequestHandler):
    @tornado.web.asynchronous
    def get(self, *args):
        self.finish()
        line = self.get_argument('line')
        lineno = self.get_argument('lineno')
        celsius = self.get_argument('celsius')
        humidity = self.get_argument('humidity')
        d_t = self.get_argument('d_t')
        data = {"line": line,"lineno":lineno, "celsius": celsius,"humidity":humidity,"d_t":d_t}
        data = json.dumps(data)
        for cl in ws_client:
            cl.write_message(data)
     
def main():
    tornado.options.parse_config_file(os.path.join(os.path.dirname(__file__), 'server.conf'))
    tornado.options.parse_command_line()
    app = Application()
    #httpServer=tornado.httpserver.HTTPServer(app)
    app.listen(options.port)
    #httpServer.listen(options.port)

    logging.debug('run on port %d in %s mode' % (options.port, options.logging))
    tornado.ioloop.IOLoop.instance().start()

def mysql_db_cursor():
    con = MySQLdb.connect(host='mushroominstance.cxneilncg8ry.ap-northeast-1.rds.amazonaws.com',
                          db='mushroom_db',
                          user='mushroom_db_user',
                          passwd='mmn39504',
                          port=3306)  
    cursor = con.cursor()
    return cursor
      
    
if __name__ == "__main__":
    main()