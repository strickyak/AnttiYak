#!/usr/bin/python
#
# http://localhost:30332/?f=scan&c=555
# 
# http://localhost:30332/?f=create&c=555&i=668&u=strick&value=Banana
# 
# http://localhost:30332/?f=fetch&c=555&i=668.strick

import cgi
import glob
import subprocess
import sys
import traceback
import urlparse

from BaseHTTPServer import HTTPServer
from BaseHTTPServer import BaseHTTPRequestHandler

class AHandler(BaseHTTPRequestHandler):
  def do_GET(s):
    path = s.path
    p = urlparse.urlparse(s.path)
    q = urlparse.parse_qs(p.query)

    verb = q.get('f') and q['f'][0]
    user = q.get('u') and q['u'][0]
    chan = q.get('c') and q['c'][0]
    inode = q.get('i') and q['i'][0]
    latest = q.get('latest') and q['latest'][0]
    value = q.get('value') and q['value'][0]

    try:
      if verb == 'scan':
        z = doVerbScan(chan, latest)
      elif verb == 'fetch':
        z = doVerbFetch(chan, inode)
      elif verb == 'create':
        z = doVerbCreate(chan, inode, value, user)
      else:
        z = doVerbDefault(verb)

      s.send_response(200)
      s.send_header('Content-Type', 'text/plain')
      s.end_headers()
      print >>s.wfile, z

    except:
      s.send_response(400)
      s.send_header('Content-Type', 'text/plain')
      s.end_headers()
      print >>s.wfile, "ERROR" 
      print >>s.wfile, traceback.format_exc()

def doVerbDefault(verb):
  raise Exception("Bad verb: %s", repr(verb))

def doVerbScan(chan, latest):
  print "GLOB", 'data/%s/*' % chan
  print "GLOB", glob.glob('data/%s/*' % chan)
  g = glob.glob('data/%s/*' % chan)
  z = [x.split('/')[-1] for x in g]
  return '\n'.join(z)

def doVerbFetch(chan, inode):
  f = open('data/%s/%s' % (chan, inode))
  z = f.read()
  f.close()
  return z

def doVerbCreate(chan, inode, value, user):
  f = open('data/%s/%s.%s' % (chan, inode, user), 'w')
  f.write(value)
  f.close()
  return ''

address = sys.argv[1]
serv = HTTPServer((address, 30332), AHandler)
try:
  serv.serve_forever()
except KeyboardInterrupt:
  pass
serv.server_close()
