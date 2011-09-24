import gtk
import gtk.glade

tree = gtk.glade.XML("notepad.glade")

class Notepad(object):
    def __init__(self):
        self.window = tree.get_widget("mainwindow")
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
        self.window.set_title('Notepad - %s%s' % ('Untitled' if self.name is None else self.name, '*' if self.dirty else ''))

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
        print "A"
        self.calc.handle_ask(False)
        self.window.destroy()
    def on_save(self, widget):
        print "B"
        self.calc.handle_ask(True)
        self.window.destroy()
    def on_cancel(self, widget):
        print "C"
        self.window.destroy()
    def destroy(self, widget):
        self.calc.asking = False
        

calc = Notepad()
gtk.main()
