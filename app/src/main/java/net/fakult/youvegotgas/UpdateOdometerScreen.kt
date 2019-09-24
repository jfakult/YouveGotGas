package net.fakult.youvegotgas

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_update_odometer_screen.*
import kotlin.math.abs
import kotlin.math.pow

const val TAG : String = "UpdateOdoScreen"
class UpdateOdometerScreen : AppCompatActivity()
{
    private var updatedOdometer: Int = -1
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_odometer_screen)

        val currentOdometer: Int = loadOdometer(applicationContext as Activity) // Get from either shared prefs or firebase (prob load all from firebase on start, update sharedprefs immediately to have local copy)
        updatedOdometer = currentOdometer


        val odoPicker: NumberPicker = odometerPicker
        val submitButton: Button = submitButton
        val expandingSubmit = expandingSubmit

        odoPicker.minValue = 0
        odoPicker.maxValue = 9999999 // 0- 10 mil
        odoPicker.value = 10000

        odoPicker.setOnValueChangedListener { _, _, newVal ->
            //Underscores for unused params
            updatedOdometer = newVal
        }

        submitButton.setOnClickListener {
            //Toast.makeText(applicationContext, "Press and hold to confirm")...
            //Do nothing actually
        }

        submitButton.setOnTouchListener(SubmitTouchListener(expandingSubmit, applicationContext as Activity))

        databaseReference = FirebaseDatabase.getInstance().reference

    }

    inner class SubmitTouchListener(eE: TextView, act : Activity) : View.OnTouchListener
    {
        private lateinit var expandableElement: TextView
        private val expandSpeed = 1300L // ms
        private val shrinkSpeed = expandSpeed / 3
        private var expandAnim: ScaleAnimation
        private var shrinkAnim: ScaleAnimation
        private var expandStartTime: Long = 0

        private val maxSize = 1.9f

        init
        {
            expandableElement = eE
            val activity : Activity = act

            expandAnim = ScaleAnimation(1f,
                                        maxSize,
                                        1f,
                                        maxSize, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            shrinkAnim = ScaleAnimation(maxSize,
                                        1f,
                                        maxSize,
                                        1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

            expandAnim.interpolator = LinearInterpolator()
            shrinkAnim.interpolator = LinearInterpolator()
            expandAnim.duration = expandSpeed
            shrinkAnim.duration = expandSpeed
            expandAnim.setAnimationListener(object : Animation.AnimationListener
                                            {
                                                override fun onAnimationRepeat(animation: Animation?)
                                                {
                                                    // Do nothing
                                                }

                                                override fun onAnimationEnd(animation: Animation?)
                                                {
                                                    if (System.currentTimeMillis() - expandStartTime >= (expandSpeed - 10))
                                                    {
                                                        Toast.makeText(expandableElement.context, "Submitting!", Toast.LENGTH_SHORT)
                                                            .show()

                                                        Thread.sleep(1200)

                                                        updateOdometer(activity)
                                                    }
                                                    expandStartTime = 2.0.pow(62.0).toLong() // A Big number
                                                }

                                                override fun onAnimationStart(animation: Animation?)
                                                {
                                                    expandStartTime = System.currentTimeMillis()
                                                }
                                            })
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean
        {
            if (expandableElement.animation == null)
            {
                expandableElement.startAnimation(expandAnim)
            }

            if (event?.action == MotionEvent.ACTION_DOWN)
            {
                // Start expanding expandingSubmit

                expandableElement.clearAnimation()
                expandableElement.startAnimation(expandAnim)
            }
            if (event?.action == MotionEvent.ACTION_UP)
            {
                // If we held the button long enough, submit the data
                val timeHeld = System.currentTimeMillis() - expandStartTime

                if (timeHeld >= expandSpeed)
                {
                    Toast.makeText(expandableElement.context, "Submitting!", Toast.LENGTH_SHORT)
                        .show()
                }
                else
                {
                    // Quickly shrink the expandingSubmit section

                    //expandableElement.animation.interpolator = ReverseInterpolator()
                    //expandableElement.animation.duration = shrinkSpeed

                    //expandableElement.clearAnimation()
                    val completeRatio = (timeHeld / expandSpeed.toDouble())
                    val newSize = 1 + (maxSize - 1) * completeRatio
                    shrinkAnim = ScaleAnimation(newSize.toFloat(),
                                                1f,
                                                newSize.toFloat(),
                                                1f,
                                                Animation.RELATIVE_TO_SELF,
                                                0.5f,
                                                Animation.RELATIVE_TO_SELF,
                                                0.5f)
                    shrinkAnim.duration = shrinkSpeed
                    shrinkAnim.interpolator = LinearInterpolator()

                    expandableElement.startAnimation(shrinkAnim)
                }
            }

            return true
        }
    }

    private fun loadOdometer(activity : Activity) : Int
    {
        val odo = activity.getPreferences(Context.MODE_PRIVATE).getInt("odometer", -1)

        if ( odo >= 0 )
        {
            return odo
        }

        Log.d(TAG, "Looking up odo from firebase")
        //Load from firebase
        //Temporarily disabled as async requests are difficult at best, may not be worth the implementation
        /*databaseReference.child("odometer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError)
            {
                // Ignore
            }

            override fun onDataChange(p0: DataSnapshot)
            {

            }
        })*/

        return 10000
    }

    fun updateOdometer(activity : Activity)
    {
        activity.getPreferences(Context.MODE_PRIVATE).edit().putInt("odometer", updatedOdometer).apply()

        //Upload result to firebase asynchronously as well
        databaseReference.setValue("odometer", updatedOdometer)
    }
}
