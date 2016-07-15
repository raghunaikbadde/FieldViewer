package com.lanesgroup.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class GravityCompoundDrawable extends Drawable {

	// inner Drawable
	private final Drawable mDrawable;

	public GravityCompoundDrawable(Drawable drawable) {
		mDrawable = drawable;
	}

	@Override
	public int getIntrinsicWidth() {
		return mDrawable.getIntrinsicWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return mDrawable.getIntrinsicHeight();
	}

	@Override
	public void draw(Canvas canvas) {
		int halfCanvas = canvas.getHeight() / 2;
		int halfDrawable = mDrawable.getIntrinsicHeight() / 2;

		// align to top
		canvas.save();
		canvas.translate(0, -halfCanvas + halfDrawable);
		mDrawable.draw(canvas);
		canvas.restore();
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}
}