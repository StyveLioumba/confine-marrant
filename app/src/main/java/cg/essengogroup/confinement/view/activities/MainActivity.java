package cg.essengogroup.confinement.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;

import cg.essengogroup.confinement.R;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView image;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        image=findViewById(R.id.imgLogo);
        linearLayout=findViewById(R.id.lineBottom);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
               /* //Call next screen
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                // Attach all the elements those you want to animate in design
                Pair[]pairs=new Pair[1];
//                pairs[0]=new Pair<View, String>(image,"logo_image");
                pairs[0]=new Pair<View, String>(linearLayout,"logo_text");
                //wrap the call in API level 21 or higher
                if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.LOLLIPOP)
                {
                    ActivityOptions options= ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                    startActivity(intent,options.toBundle());
                    finish();
                }*/
               startActivity(new Intent(getApplicationContext(),LoginActivity.class));
               finish();
            }
        },4000);
    }
}
