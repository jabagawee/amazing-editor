from diff_match_patch import diff_match_patch

class TextBuffer(object):
    def __init__(self,text_buff):
        self.text_buff = text_buff
    def get_text(self):
        start = self.text_buff.get_start_iter()
        end = self.text_buff.get_end_iter()
        return self.text_buff.get_text(start,end)
    def set_text(self,text):
        self.text_buff.set_text(text)

diffy_matchy_patchy = diff_match_patch()
class PatchLogger(object):
    def __init__(self,text_buff,conn):
        self.text_buff = text_buff
        self.old_text = text_buff.get_text()
        self.patches = []
        self.back_patches = []
        self.patch_index = 0 #before first patch
        self.conn = conn
    def init_diffs(self,filename):
        self.patches = self.conn.get_start_diffs(filename)
        old_text = self.conn.read_file(filename,0)
        new_text = old_text
        while(self.patch_index < len(self.patches)):
            patches = self.patches[self.patch_index]
            self.patch_index += 1
            new_text = diffy_matchy_patchy.patch_apply(patches, old_text)[0]
            if(len(self.back_patches) < self.patch_index):
                back = diffy_matchy_patchy.patch_make(new_text, old_text)
                self.back_patches.append(back)
            old_text = new_text
            self.old_text = new_text
        self.text_buff.set_text(new_text)
    def add_scrub_bar(self,scrub_bar):
        self.scrub_bar = scrub_bar
        self.scrub_bar.set_scrub_length(len(self.patches))
        self.scrub_bar.set_scrub_index(self.patch_index)
    def check_send_patch(self,*args):
        new_text = self.text_buff.get_text()
        patches = diffy_matchy_patchy.patch_make(self.old_text, new_text)
        patch_text = diffy_matchy_patchy.patch_toText(patches)
        if(len(patch_text) > 0):
            self.conn.send_diff(patch_text)
            self.patches.append(patches)
            back = diffy_matchy_patchy.patch_make(new_text, self.old_text)
            self.back_patches.append(back)
            self.patch_index += 1
            self.scrub_bar.set_scrub_length(len(self.patches))
            self.scrub_bar.set_scrub_index(self.patch_index)
            print("sending")
        self.old_text = new_text
        return True
    def server_recieve(self,patch_text):
        print("got update")
        patches = diffy_matchy_patchy.patch_fromText(patch_text)
        self.patches.append(patches)
        self.scrub_bar.set_scrub_length(len(self.patches))
        if(len(self.patches) - 1 == self.patch_index):
            self.move_forward()
            self.scrub_bar.set_scrub_index(self.patch_index)
    def move_forward(self):
        if(self.patch_index < len(self.patches)):
            patches = self.patches[self.patch_index]
            self.patch_index += 1
            old_text = self.text_buff.get_text()
            new_text = diffy_matchy_patchy.patch_apply(patches, old_text)[0]
            self.text_buff.set_text(new_text)
            if(len(self.back_patches) < self.patch_index):
                back = diffy_matchy_patchy.patch_make(new_text, old_text)
                self.back_patches.append(back)
            self.old_text = new_text
            return True
        return False

    def move_backward(self):
        if(self.patch_index > 0):
            self.patch_index -= 1
            patches = self.back_patches[self.patch_index]
            new_text,error = diffy_matchy_patchy.patch_apply(patches, 
                    self.text_buff.get_text())
            self.text_buff.set_text(new_text)
            self.old_text = new_text



        


        
