bind = '127.0.0.1:8000'
workers = 5
backlog = 2048
worker_class = 'gevent'
debug = True
daemon = True
pidfile = 'gunicorn.pid'
logfile = 'gunicorn.log'