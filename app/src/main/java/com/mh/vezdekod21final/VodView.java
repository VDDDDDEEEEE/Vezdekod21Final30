package com.mh.vezdekod21final;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.give.vezdekodmh.utils.L;

public class VodView extends VideoView {

	public VodView(Context context) {
		super(context);
	}

	public VodView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VodView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	/**
	 * Resize video view by using SurfaceHolder.setFixedSize(...). See {@link android.view.SurfaceHolder#setFixedSize}
	 * @param width
	 * @param height
	 */
	public void setFixedVideoSize(int width, int height)
    {
		L.INSTANCE.d("setFixedVideoSize width = " + width );
		L.INSTANCE.d("setFixedVideoSize height = " + height );
        getHolder().setFixedSize(width, height);
    } 
}

