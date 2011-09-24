#! /usr/bin/env python

import gtk
import gtk.glade
from gtkcodebuffer import CodeBuffer, SyntaxLoader
from diff_match_patch import diff_match_patch
import sys
import socket
from random import randint

def n2b(n):
    return bytearray((n / 2 ** 24, n % 2 ** 24 / 2 ** 16, n % 2 ** 16 / 2 ** 8, n % 2 ** 8))
b = bytearray

server = '10.10.65.150'
port = 19999
user = 'user' + str(randint(1, 1000))
password = 'cookies'

lang = SyntaxLoader('python')
buff = CodeBuffer(lang=lang)

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((server, int(port)))

msg = b('a') + n2b(len(user)) + b(user) + n2b(len(password)) + b(password)
s.send(msg)
print s.recv(1024)

msg = b('q') + n2b(0)
s.send(msg)
files = s.recv(1024)[5:].split(';')

tree = gtk.glade.XML("notepad.glade")

class Notepad(object):
    def __init__(self):
        self.window = tree.get_widget("mainwindow")
        tree.get_widget("text").set_buffer(buff)
        self.text = tree.get_widget('text').get_buffer()
        
        self.window.connect("delete_event", self.on_delete)
        self.window.connect("destroy", self.on_destroy)
        tree.get_widget("new").connect("activate", self.on_new)
        tree.get_widget("quit").connect("activate", self.on_destroy)
        self.text.connect("changed", self.on_change)

        self.name = None
        self.dirty = False
        self.update_title()
        self.asking = False
        
        self.window.show()
    
    def on_change(self, widget):
        self.dirty = True
        self.update_title()
    
    def on_new(self, widget):
        self.name = None
        self.text.set_text('')
        self.dirty = False
        self.update_title()
    
    def on_delete(self, widget, event):
        print 'delete'
        if self.dirty:
            if not self.asking:
                Ask(self)
                self.asking = True
            return True
        return False
    
    def on_destroy(self, widget):
        gtk.main_quit()
     
    def update_title(self):
        self.window.set_title('awesome-editor - %s%s' % ('Untitled' if self.name is None else self.name, '*' if self.dirty else ''))

class Ask(object):
    def __init__(self, calc):
        self.calc = calc
        
        self.window = tree.get_widget("askwindow")
        self.window.connect("destroy", self.destroy)
        tree.get_widget("dialog_close").connect("activate", self.on_close)
        tree.get_widget("dialog_cancel").connect("activate", self.on_cancel)
        tree.get_widget("dialog_save").connect("activate", self.on_save)
        self.window.show()
    def on_close(self, widget):
        self.calc.handle_ask(False)
        self.window.destroy()
    def on_save(self, widget):
        self.calc.handle_ask(True)
        self.window.destroy()
    def on_cancel(self, widget):
        self.window.destroy()
    def destroy(self, widget):
        self.calc.asking = False

pad = Notepad()

filename = 'test1.txt'
ver_num = 13

msg = b('r') + n2b(len(filename)) + b(filename) + n2b(ver_num)
s.send(msg)
pad.text.set_text(s.recv(102400)[5:])

msg = b('e') + n2b(len(filename)) + b(filename) + n2b(ver_num) 
s.send(msg)
pad.text.set_text(s.recv(102400)[5:])

def get_goddamn_text():
    return pad.text.get_text(pad.text.get_start_iter(), pad.text.get_end_iter())

old_text = get_goddamn_text()

diffy_matchy_patchy = diff_match_patch()

def communicate_with_server(*args):
    '''Fuck you Stephen'''
    # patch the old text
    global old_text
    new_text = get_goddamn_text()
    patches = diffy_matchy_patchy.patch_make(old_text, new_text)
    patch_text = diffy_matchy_patchy.patch_toText(patches)
    print patch_text

    # send to server
    msg = b('d') + n2b(len(patch_text)) + b(patch_text)
    s.send(msg)
    s.recv(1024)

    # receive new shit
    msg = b('g') + n2b(0)
    s.send(msg)
    response = s.recv(1024)

    # apply patches to server
    if response[0] == 'd':
        patches = diffy_matchy_patchy.patch_fromText(response[5:])
        pad.text.set_text(diffy_matchy_patchy.patch_apply(patches, get_goddamn_text())[0])

    old_text = get_goddamn_text()

    # done
    return True

    # should never run
    return False


gtk.timeout_add(50, communicate_with_server) # every second

gtk.main()
