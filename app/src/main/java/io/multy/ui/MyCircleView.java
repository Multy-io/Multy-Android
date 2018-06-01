/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

import butterknife.BindArray;
import io.multy.R;

public class MyCircleView extends View {

    private Bitmap original;
    private Bitmap mask;
    private Random random = new Random();
    private int height;
    private int width;
    private int cx;
    private int cy;

    public MyCircleView(Context context) {
        super(context);
    }

    public MyCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMask(canvas);
    }

    private void init() {
        int w = getWidth();
        int h = getHeight();

        int pl = getPaddingLeft();
        int pr = getPaddingRight();
        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        width = w - (pl + pr);
        height = h - (pt + pb);

        cx = pl + (width / 2);
        cy = pt + (height / 2);
    }

    public void drawMask(Canvas canvas) {
        if (original == null) {
            init();
            original = generateBitmap();
        }

        if (mask == null) {
//            mask = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.white_circle_mask);
            mask = generateMask();
        }

        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Canvas tempCanvas = new Canvas(result);
        tempCanvas.drawBitmap(original, 0, 0, null);
        tempCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);

        canvas.drawBitmap(result, 0, 0, new Paint());
    }

    private Bitmap generateMask() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, Math.min(width, height) / 2, paint);
        return bitmap;
    }

    private Bitmap generateBitmap() {
        final int[] colors = getContext().getResources().getIntArray(R.array.circle_colors);
        final int maxRandom = colors.length - 1;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.rotate(random.nextInt(360), cx, cy);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(colors[random.nextInt(maxRandom)]);

        final int tx = random.nextInt(width - width / 3);
        final int ty = random.nextInt(height - height / 3);

        paint.setColor(colors[random.nextInt(maxRandom)]);
        canvas.drawRect(0, 0, tx, ty, paint);

        paint.setColor(colors[random.nextInt(maxRandom)]);
        canvas.drawRect(cx, 0, tx, ty, paint);

        paint.setColor(colors[random.nextInt(maxRandom)]);
        canvas.drawRect(cx, cy, tx, ty, paint);

        paint.setColor(colors[random.nextInt(maxRandom)]);
        canvas.drawRect(0, cy, tx, ty, paint);

        return bitmap;
    }
}
