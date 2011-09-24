#include "XTermFont.h"

#include "chars.x"
#include "bold_chars.x"
void FontContext::DrawGlyph(int x_loc,int y_loc, struct xfont_char *glyph){
        //int clip_x1 = 0;//xtext->_clip.x;
        //int clip_x2 = clip_x1 + xtext->_clip.width;
        //int clip_y1 = xtext->_clip.y;
        //int clip_y2 = clip_y1 + xtext->_clip.height;
        int i;
        int length = glyph->length;
        int j = 0;
        GdkPoint my_points[length];
        //if(x_loc < clip_x1 - 6 || y_loc < clip_y1 - 13) 
        //        return;
        //if(x_loc > clip_x2 || y_loc > clip_y2)
        //        return;
        for(i = 0;i<length;i++){
                int x = glyph->coords[i].x + x_loc;
                int y = glyph->coords[i].y + y_loc;
          //      if(x >= clip_x1 && y >= clip_y1 &&
            //                    x <= clip_x2 && y <= clip_y2){
                        my_points[j].x = x;
                        my_points[j].y = y;
                        j ++; 
              //  }   
        }   
        if(j > 0)
                DrawPoints(my_points , j); 
}
void FontContext::DrawPoints(GdkPoint *points,int length){
	gdk_draw_points(_window,_gc,points,length);
}
void FontContext::TestDraw(){
	DrawBoldCode('a',0,0);
	DrawBoldCode('b',10,10);
}
gboolean FontContext::DrawBoldCode(int code,int x_loc,int y_loc){
	if((code >= 32) && (code < 127)){
		struct xfont_char *glyph = bold_xfont_chars + code - 32;
 		DrawGlyph(x_loc,y_loc,glyph);
		return TRUE;
	}else{
		return FALSE;
	}
}
void FontContext::SetDrawingFG(int r,int g,int b){
	GdkColor c;
	c.pixel = 0;
	c.red = r * 256;
	c.green = g * 256;
	c.blue = r * 256;
	gdk_gc_set_rgb_fg_color(_gc,&c);
}
FontContext::FontContext(GdkDrawable *drawable){
	_gc = gdk_gc_new(drawable);
	_window = drawable;

}
