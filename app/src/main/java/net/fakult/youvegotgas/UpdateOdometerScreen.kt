package net.fakult.youvegotgas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.activity_update_odometer_screen.*
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.TextView
import android.widget.Toast


class UpdateOdometerScreen : AppCompatActivity()
{
    var updatedOdometer : Int = -1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_odometer_screen)

        val currentOdometer : Int = 100 // Get from either shared prefs or firebase (prob load all from firebase on start, update sharedprefs immediately to have local copy)
        updatedOdometer = currentOdometer


        val odoPicker : NumberPicker = odometerPicker
        val submitButton : Button = submitButton
        val expandingSubmit = expandingSubmit

        odoPicker.minValue = 0
        odoPicker.maxValue = 9999999 // 0- 10 mil
        odoPicker.value = 10000

        odoPicker.setOnValueChangedListener { _, _, newVal -> //Underscores for unused params
            updatedOdometer = newVal
        }

        submitButton.setOnClickListener {
            //Toast.makeText(applicationContext, "Press and hold to confirm")...
            //Do nothing actually
        }

        submitButton.setOnTouchListener(SubmitTouchListener(expandingSubmit))
    }

    fun submitMileage(odo : NumberPicker)
    {
        //Update old val to new val
        //Write new val to sharedprefs, firebase
    }

    private class SubmitTouchListener(expandableElement: TextView) : View.OnTouchListener
    {
        lateinit var expandableElement: TextView
        val expandSpeed = 400L // ms
        val shrinkSpeed = expandSpeed / 3
        lateinit var expandAnim : ScaleAnimation
        lateinit var shrinkAnim : ScaleAnimation

        fun SubmitTouchListener(eE : TextView)
        {
            expandableElement = eE

            expandAnim = ScaleAnimation(1f, 1.9f, 1f, 1.9f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f)
            shrinkAnim = ScaleAnimation(1.9f, 1f, 1.9f, 1f, Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_PARENT, 1f)

            expandAnim.interpolator = AccelerateDecelerateInterpolator()
            shrinkAnim.interpolator = AccelerateDecelerateInterpolator()

            expandAnim.duration = expandSpeed
            shrinkAnim.duration = expandSpeed

            expandAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?)
                {
                    // Do nothing
                }

                override fun onAnimationEnd(animation: Animation?)
                {
                    // Submit the updated odometer
                    Toast.makeText(expandableElement.context, "Submitting!", Toast.LENGTH_SHORT).show()
                }

                override fun onAnimationStart(animation: Animation?)
                {
                    // Do nothing
                }

            })
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean
        {
            if (event?.action == MotionEvent.ACTION_DOWN)
            {
                //Start expanding expandingSubmit section
                expandableElement.startAnimation(expandAnim)
            }
            if (event?.action == MotionEvent.ACTION_UP)
            {
                //Quickly shrink the expandingSubmit section
                expandableElement.startAnimation(shrinkAnim)
            }

            return true
        }
    }
}
