#!/usr/bin/python
#
# Startup script for mongod

import subprocess
import os

def start_mongo(directory, port):
    if not os.path.isdir(directory):
        if os.path.exists(directory):
            raise directory + " exists but is not a directory"
        os.mkdir(directory)
    subprocess.Popen(["mongod", "--port", port, "--dbpath", directory])

def stop_mongo(directory, port):
    subprocess.Popen(["mongod", "--port", port, "--dbpath", directory, "--shutdown"])

import argparse
parser = argparse.ArgumentParser(description="Start/Stop mongo instance")
parser.add_argument("action", choices=["start", "stop"])
parser.add_argument("--port", default="27017")
parser.add_argument("--dir", default="./data")
args = parser.parse_args()

def start():
    start_mongo(args.dir, args.port)

def stop():
    stop_mongo(args.dir, args.port)

a = { 'start': start, 'stop': stop }[args.action]
a()
