
package com.freetoolsbox.liveclock;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.util.Calendar;

public class ClockWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new ClockEngine();
    }

    private class ClockEngine extends Engine {

        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean visible = false;

        private final Runnable drawRunnable = new Runnable() {
            @Override
            public void run() {
                drawClock();
                if (visible) {
                    handler.postDelayed(this, 1000);
                }
            }
        };

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;
            handler.removeCallbacks(drawRunnable);

            if (visible) {
                handler.post(drawRunnable);
            }
        }

        @Override
        public void onSurfaceChanged(
                SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawClock();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            visible = false;
            handler.removeCallbacks(drawRunnable);
            super.onSurfaceDestroyed(holder);
        }

        private void drawClock() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas == null) return;

                int width = canvas.getWidth();
                int height = canvas.getHeight();

                canvas.drawColor(Color.rgb(18, 27, 43));

                float cx = width / 2f;
                float cy = height / 2f;
                float radius = Math.min(width, height) * 0.32f;

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, radius, paint);

                paint.setColor(Color.rgb(35, 45, 60));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(radius * 0.025f);
                canvas.drawCircle(cx, cy, radius, paint);

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.BLACK);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(radius * 0.13f);

                for (int i = 1; i <= 12; i++) {
                    double angle = Math.toRadians(i * 30 - 90);

                    float x = cx + (float) Math.cos(angle) * radius * 0.78f;
                    float y = cy + (float) Math.sin(angle) * radius * 0.78f
                            + paint.getTextSize() * 0.35f;

                    canvas.drawText(String.valueOf(i), x, y, paint);
                }

                paint.setStrokeWidth(radius * 0.018f);

                for (int i = 0; i < 60; i++) {
                    double angle = Math.toRadians(i * 6);

                    float outer = radius * 0.91f;
                    float inner = radius * (i % 5 == 0 ? 0.84f : 0.88f);

                    float x1 = cx + (float) Math.cos(angle) * inner;
                    float y1 = cy + (float) Math.sin(angle) * inner;
                    float x2 = cx + (float) Math.cos(angle) * outer;
                    float y2 = cy + (float) Math.sin(angle) * outer;

                    canvas.drawLine(x1, y1, x2, y2, paint);
                }

                Calendar time = Calendar.getInstance();

                float seconds = time.get(Calendar.SECOND)
                        + time.get(Calendar.MILLISECOND) / 1000f;
                float minutes = time.get(Calendar.MINUTE) + seconds / 60f;
                float hours = (time.get(Calendar.HOUR) % 12) + minutes / 60f;

                drawHand(canvas, cx, cy, hours * 30 - 90,
                        radius * 0.48f, radius * 0.055f, Color.BLACK);

                drawHand(canvas, cx, cy, minutes * 6 - 90,
                        radius * 0.68f, radius * 0.035f,
                        Color.rgb(30, 100, 190));

                drawHand(canvas, cx, cy, seconds * 6 - 90,
                        radius * 0.76f, radius * 0.012f, Color.RED);

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
                canvas.drawCircle(cx, cy, radius * 0.045f, paint);

            } catch (Exception e) {
                // Prevent wallpaper crashes on drawing errors.
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void drawHand(
                Canvas canvas,
                float cx,
                float cy,
                float angleDegrees,
                float length,
                float thickness,
                int color) {

            double angle = Math.toRadians(angleDegrees);

            float endX = cx + (float) Math.cos(angle) * length;
            float endY = cy + (float) Math.sin(angle) * length;

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);
            paint.setStrokeWidth(thickness);
            paint.setStrokeCap(Paint.Cap.ROUND);

            canvas.drawLine(cx, cy, endX, endY, paint);
        }
    }
}

