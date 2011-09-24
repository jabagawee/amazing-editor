import socket
from diff_match_patch import diff_match_patch
import sys

diffy_matchy_patchy = diff_match_patch()
def n2b(n):
    if(n < 0):
        return bytearray((255,255,255,255))
    return bytearray((n / 2 ** 24, n % 2 ** 24 / 2 ** 16, n % 2 ** 16 / 2 ** 8, n % 2 ** 8))
b = bytearray

def b2n(b):
    """
    >>> b2n(n2b(1))
    1
    """
    n = 0
    b = bytearray(b)
    print(tuple(b))
    k = b[0] * 2 ** 24 + b[1] * 2 ** 16 + b[2] * 2 ** 8 + b[3]
    return k
    for i in bytearray(b):
        n * (2 ** 8)
        n += i
    return n

k = 23888
print b2n(n2b(k)) == k

class Connection(object):
    def __init__(self,server,port):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((server,int(port)))
    def auth(self,user,password):
        msg = b('a') + n2b(len(user)) + b(user) + n2b(len(password)) + b(password)
        self.sock.send(msg)
        print self.sock.recv(1024)
    def get_files(self):
        msg = b('q') + n2b(0)
        self.sock.send(msg)
        return self.sock.recv(1024)[5:].split(';')
    def read_file(self,filename,ver_num):
        msg = b('r') + n2b(len(filename)) + b(filename) + n2b(ver_num)
        self.sock.send(msg)
        return(self.sock.recv(102400)[5:])
    def lock_to(self,filename,ver_num):
        msg = b('e') + n2b(len(filename)) + b(filename) + n2b(ver_num) 
        self.sock.send(msg)
        return(self.sock.recv(102400)[5:])
    def send_diff(self,patch_text):
        if(len(patch_text) > 0):
            msg = b('d') + n2b(len(patch_text)) + b(patch_text)
            self.sock.send(msg)
            self.sock.recv(1024)
    def logout(self):
        msg = b('l') + n2b(0)
        self.sock.send(msg)
    def get_start_diffs(self,filename):
        msg = b('y') + n2b(len(filename)) + b(filename)
        self.sock.send(msg)
        length = b2n(self.sock.recv(5)[1:5])
        print(length)
        diffs = ""
        while len(diffs) < length:
            diffs += self.sock.recv(length - len(diffs))
        print len(diffs)
        diff_list = []
        for patch in diffy_matchy_patchy.patch_fromText(diffs):
            diff_list.append([patch])
        return diff_list





