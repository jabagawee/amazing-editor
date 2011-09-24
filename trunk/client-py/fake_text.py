from diff_match_patch import diff_match_patch

class Text_Buffer(object):
    def __init__(self,text):
        self.text = text
    def get_text(self):
        return self.text



class Fake_Text_Object(object):
    def __init__(self,text_buffer):
        self.text_buffer = text_buffer
    def __add__(self,other):
        return other




