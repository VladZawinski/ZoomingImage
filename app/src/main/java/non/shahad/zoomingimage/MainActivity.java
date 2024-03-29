package non.shahad.zoomingimage;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    public static final String TAG = "MainActivityL";
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    public static final int NONE = 0;
    public static final int DRAG = 1;
    public static final int ZOOM = 2;
    int mode = NONE;

    PointF start = new PointF();
    PointF mid = new PointF();

    float oldDistance = 1f;

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imagev);
        mImageView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ImageView imageView = (ImageView)view;
        dumbEvent(motionEvent);

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN :
                savedMatrix.set(matrix);
                start.set(motionEvent.getX(),motionEvent.getY());
                Log.i(TAG, "mode=DRAG");
                mode = DRAG;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.i(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG){
                    matrix.set(savedMatrix);
                    matrix.postTranslate(motionEvent.getX() - start.x,motionEvent.getY() - start.y);
                }else if (mode == ZOOM){
                    float newDistance = spacing(motionEvent);
                    Log.i(TAG, "new Distance= " + newDistance);
                    if (newDistance > 10f){
                        matrix.set(savedMatrix);
                        float scale = newDistance / oldDistance;
                        matrix.postScale(scale,scale,mid.x,mid.y);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDistance = spacing(motionEvent);
                Log.i(TAG, "old distance " + oldDistance);
                if (oldDistance > 10f){
                    savedMatrix.set(matrix);
                    midPoint(mid,motionEvent);
                    mode = ZOOM;
                    Log.i(TAG, "mode=ZOOM");
                }
                break;

        }

        imageView.setImageMatrix(matrix);
        return true;
    }


    private void dumbEvent(MotionEvent event){
        String name[] = {"DOWN","UP","MOVE","CANCEL","OUTSIDE","POINTER_DOWN","POINTER_UP","7?","8?","9?"};

        StringBuilder sb = new StringBuilder();

        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;

        sb.append("event ACTION_").append(name[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");

        for (int i = 0; i< event.getPointerCount(); i++){
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int)event.getX(i));
            sb.append(",").append((int)event.getY(i));

            if (i + 1 < event.getPointerCount()){
                sb.append(";");
            }

        }

        sb.append("]");

        Log.i(TAG, "dumbEvent: " + sb.toString());

    }

    // used formula for euclidean distance
    private float spacing(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float)Math.sqrt(x * x - y * y);
    }

    private void midPoint(PointF point , MotionEvent event){
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);

        point.set(x/2,y/2);
    }
}
