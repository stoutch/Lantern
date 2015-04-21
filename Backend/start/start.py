#!/usr/bin/python2.7
#
# Startup script for tornado

import sys
import argparse
import config

sys.path.append('../')


parser = argparse.ArgumentParser(description='Start/Stop MAS server')
parser.add_argument('action', choices=['start', 'stop'])
parser.add_argument('--env', default='dev')

args = parser.parse_args()
conf = config.envs[args.env]

def start_server():
    import server
    server.start(conf)

def stop_server():
    from httplib import HTTPConnection
    conn = HTTPConnection('localhost', conf.tornado_port)
    conn.request(method='GET', url='/stop')
    resp = conn.getresponse()
    if resp.status != 200:
        print resp.msg
        exit (1)

a = {'start': start_server, 'stop': stop_server}[args.action]
a()
