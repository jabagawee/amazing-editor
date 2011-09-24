#ifndef _XTERM_FONT_H_
#define _XTERM_FONT_H_
//#include <cairo.h>
#include <gtk/gtk.h>

struct xfont_char{
	int length;
	GdkPoint coords[64];
};

class FontContext{
	public:
		FontContext(GdkDrawable *drawable);
		void SetDrawingFG(int r,int g,int b);
		void DrawGlyph(int x_loc,int y_loc,struct xfont_char *glyph);
		void TestDraw();
		void DrawPoints(GdkPoint *points,int length);
		gboolean DrawBoldCode(int code,int x_loc,int y_loc);
	private:
		GdkGC *_gc;
		GdkDrawable *_window;
};


#endif
