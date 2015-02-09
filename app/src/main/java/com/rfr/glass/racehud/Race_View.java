package com.rfr.glass.racehud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.text.NumberFormat;

/** Created by game1_000 on 1/29/2015. */

public class Race_View extends View
{
    private static final String LIVE_CARD_TAG = "Racev3_View";

    private final Bitmap mRaceGauge;
    private final Bitmap mRaceNeedle;
    private final Bitmap mRaceDot;
    private final Bitmap resizedGauge;
    private final Bitmap resizedNeedle;
    private final Bitmap resizedDot;

    private final int viewWidth;
    private final int viewHeight;

    private float RaceGaugeW;
    private float RaceGaugeH;
    private float resizedGaugeW;
    private float resizedGaugeH;
    private float RaceDotW;
    private float RaceDotH;
    private float resizedDotW;
    private float resizedDotH;
    private float RaceNeedleW;
    private float RaceNeedleH;
    private float resizedNeedleW;
    private float resizedNeedleH;

    private float bitmapScalarW;
    private float bitmapScalarH;

    private static final float TEXT_SIZE_SPEED = 70f;
    private static final float TEXT_SIZE_LABEL = 30f;
    private Paint mSpeedPaint = new Paint();
    private Paint mLabelPaint = new Paint();

    private String mSpeedText;
    private String mLabelText;

    private final NumberFormat mRaceFormat;

    private GPSManager mGPSManager;

    private double mSpeedMPH;
    private float mAngle;

    public Race_View(Context context)
    {
        this(context, null, 0);
    }

    public Race_View(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public Race_View(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        mSpeedPaint.setStyle(Paint.Style.FILL);
        mSpeedPaint.setColor(Color.RED);
        mSpeedPaint.setAntiAlias(true);
        mSpeedPaint.setTextSize(TEXT_SIZE_SPEED);
        mSpeedPaint.setTextAlign(Paint.Align.CENTER);
        //mSpeedPaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        mSpeedPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets() , "fonts/Let's go Digital Regular.ttf"));
        mSpeedPaint.setAlpha(255);

        mLabelPaint.setStyle(Paint.Style.FILL);
        mLabelPaint.setColor(Color.WHITE);
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setTextSize(TEXT_SIZE_LABEL);
        mLabelPaint.setTextAlign(Paint.Align.CENTER);
        mLabelPaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        mLabelPaint.setAlpha(255);

        mSpeedText = context.getResources().getString(R.string.initial_speed);
        mLabelText = context.getResources().getString(R.string.MPH);

        mRaceFormat = NumberFormat.getNumberInstance();
        mRaceFormat.setMinimumFractionDigits(0);
        mRaceFormat.setMaximumFractionDigits(0);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        viewWidth = metrics.widthPixels;
        viewHeight = metrics.heightPixels;

        mRaceGauge = BitmapFactory.decodeResource(context.getResources(), R.drawable.racehud_gauge);
        RaceGaugeW = mRaceGauge.getWidth();
        RaceGaugeH = mRaceGauge.getHeight();

        /** This Uses entire Canvas area */
        //resizedGauge = Bitmap.createScaledBitmap(mRaceGauge, viewWidth, viewHeight, true);
        /** This Does Not Stretch Images */
        float scaleSize = viewHeight/RaceGaugeH;
        resizedGauge = Bitmap.createScaledBitmap(mRaceGauge, (int)(RaceGaugeW * scaleSize), viewHeight, true);

        resizedGaugeW = resizedGauge.getWidth();
        resizedGaugeH = resizedGauge.getHeight();

        bitmapScalarW = resizedGaugeW / RaceGaugeW;
        bitmapScalarH = resizedGaugeH / RaceGaugeH;

        mRaceDot = BitmapFactory.decodeResource(context.getResources(), R.drawable.racehud_circle);
        RaceDotW = mRaceDot.getWidth();
        RaceDotH = mRaceDot.getHeight();
        resizedDot = Bitmap.createScaledBitmap(mRaceDot,(int) (RaceDotW*bitmapScalarW), (int) (RaceDotW*bitmapScalarH), true);
        resizedDotW = resizedDot.getWidth();
        resizedDotH = resizedDot.getHeight();

        mRaceNeedle = BitmapFactory.decodeResource(context.getResources(), R.drawable.racehud_needle_large);
        RaceNeedleW = mRaceNeedle.getWidth();
        RaceNeedleH = mRaceNeedle.getHeight();
        resizedNeedle = Bitmap.createScaledBitmap(mRaceNeedle, (int) (RaceNeedleW*bitmapScalarW), (int) (RaceNeedleH*bitmapScalarH), true);
        resizedNeedleW = resizedNeedle.getWidth();
        resizedNeedleH = resizedNeedle.getHeight();

    }

    /** Sets the instance of {@link com.rfr.glass.racehud.GPSManager} that this
     * this view will use to get the current location and speed */
    public void setGPSManager(GPSManager gpsManager)
    {
        mGPSManager = gpsManager;
    }

    /** Set the speed of the speedometer. */
    public void setSpeed(double speedmph)
    {
        mSpeedMPH = speedmph;
        /** Redraw the output. */
        //invalidate();
    }

    public double getSpeedMPH()
    {
        return mSpeedMPH;
    }

    public void setAngle(double speedmph)
    {
        mAngle = (float)(1.6806f*speedmph) - 0.5619f;
        //Log.d(LIVE_CARD_TAG, "Angle is: " + String.valueOf(mRaceFormat.format(getAngle())));
        /** Redraw the output. */
        invalidate();
    }

    public float getAngle()
    {
        return mAngle;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float CenterX = canvas.getWidth() / 2.0f;
        float CenterY = canvas.getHeight() / 2.0f;

        /** Draw Gauge */
        canvas.drawBitmap(resizedGauge,CenterX - (resizedGaugeW/2),CenterY - (resizedGaugeH /2), null);

        if (getSpeedMPH()>= 0)
        {
            /** Update Output Text */
            mSpeedText = String.valueOf(mRaceFormat.format(getSpeedMPH()));
            //Log.d(LIVE_CARD_TAG, "Speed is: " + mSpeedText);
        }
        else
        {
            /** Update Output Text */
            mSpeedText = "@string/initial_speed";
            //Log.d(LIVE_CARD_TAG, "Speed is: " + mSpeedText);
        }

        /** Draw rotated needle */
        canvas.save(); // Save position of canvas
        canvas.rotate(getAngle(), CenterX, CenterY); //Rotate the canvas
        canvas.drawBitmap(resizedNeedle, CenterX - (resizedNeedleW/2), CenterY - (resizedNeedleH/2), null); //Draw needle on the rotated canvas
        canvas.restore(); //Rotate the canvas back

        /** Update the Text Background */
        canvas.drawBitmap(resizedDot,CenterX - (resizedDotW/2),CenterY - (resizedDotH/2), null);

        /** Update the text alpha and draw the text on the canvas. */
        canvas.drawText(mSpeedText, CenterX, CenterY, mSpeedPaint);
        canvas.drawText(mLabelText, CenterX, CenterY + TEXT_SIZE_LABEL, mLabelPaint);

        /** Call the next frame */
        invalidate();
    }
}
