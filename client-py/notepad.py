#! /usr/bin/env python

import gtk
import gtk.glade
from gtkcodebuffer import CodeBuffer, SyntaxLoader
from diff_match_patch import diff_match_patch
import gobject
import sys
import socket
from random import randint
import connection

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
    for i in bytearray(b):
        n * (2 ** 8)
        n += i
    return n


server = '10.10.65.150'
port = 19999
conn = connection.Connection(server,port)
user = 'user' + str(randint(1, 1000))
password = 'cookies'
conn.auth(user,password)
s = conn.sock

lang = SyntaxLoader('python')
buff = CodeBuffer(lang=lang)


files = conn.get_files()
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
                #Ask(self)
                self.asking = True
            return False
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
        gtk.main_quit()
    def on_save(self, widget):
        self.calc.handle_ask(True)
        self.window.destroy()
    def on_cancel(self, widget):
        self.window.destroy()
    def destroy(self, widget):
        self.calc.asking = False

pad = Notepad()

filename = 'test1.txt'
ver_num = -1


#msg = b('e') + n2b(len(filename)) + b(filename) + n2b(ver_num) 
pad.text.set_text(conn.lock_to(filename,-1))

import diffapply
buff = diffapply.TextBuffer(pad.text)
patch_logger = diffapply.PatchLogger(buff,conn)
patch_logger.init_diffs(filename)

class Scrubber(object):
    def __init__(self):
        self.scrub_bar = tree.get_widget("scrub_bar")
        self.adj = self.scrub_bar.get_adjustment()
        self.adj.lower = 0
        self.adj.upper = 1
        self.adj.step_increment = 1
        self.adj.page_size = 1
        self.old_value = self.adj.value = self.adj.upper - 1
        self.scrub_bar.set_update_policy(gtk.UPDATE_CONTINUOUS)
        self.scrub_bar.connect("value_changed", self.scrub)
        patch_logger.add_scrub_bar(self)
        #tree.get_widget("scrub_start").connect("activate", self.scrub_start)
        #tree.get_widget("scrub_end").connect("activate", self.scrub_end)
    def scrub_start(self,*args):
        while(patch_logger.move_forward()):
            pass
        self.scrub_bar.hide()
    def scrub_end(self,*args):
        self.scrub_bar.show()
    def scrub(self,extra):
        error = self.adj.value % 1.0
        if(error > 0.001 and error < 0.999):
            new_value = self.adj.value = int(self.adj.value + 0.5)
            self.scrub_to(new_value)
    def scrub_to(self,new_value):
        while(patch_logger.patch_index != new_value):
            if(new_value > patch_logger.patch_index):
                patch_logger.move_forward()
            elif(new_value < patch_logger.patch_index):
                patch_logger.move_backward()
    def set_scrub_length(self,length):
        self.adj.upper = length + 1
    def set_scrub_index(self,patch_index):
        self.adj.value = patch_index
Scrubber()

def handle_data(source, condition):
    print("got update")
    response = source.recv(1024)
    if len(response) > 0:
        if response[0] == 'd':
            print(b2n(response[1:5]) , len(response[5:]))
            patch_text = response[5:]
            patch_logger.server_recieve(patch_text)
        return True
    else:
        return False

gobject.io_add_watch(conn.sock, gobject.IO_IN, handle_data)

gobject.timeout_add(50, patch_logger.check_send_patch) # every second
try:
    gtk.main()
except:
    pass
print("logout")
conn.logout()
