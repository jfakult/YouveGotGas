package net.fakult.youvegotgas

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.alexzaitsev.meternumberpicker.MeterView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_update_odometer_screen.*
import kotlin.math.pow

const val TAG: String = "UpdateOdoScreen"

class UpdateOdometerScreen : AppCompatActivity()
{
    private var updatedOdometer: Int = -1
    private lateinit var databaseReference: DatabaseReference
    private lateinit var activity: Activity
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_odometer_screen)

        activity = this

        val action = savedInstanceState?.get("action")
        val dataManager = DataManager(this)

        val currentOdometer = dataManager.getData("odometer", -1, null) as Int // Get from either shared prefs or firebase (prob load all from firebase on start, update sharedprefs immediately to have local copy)
        updatedOdometer = currentOdometer

        //val odoPicker: NumberPicker = odometerPicker
        val odoPicker: MeterView = odometerPicker
        val submitButton: Button = submitButton
        val expandingSubmit = expandingSubmit

        odoPicker.value = currentOdometer

        //odoPicker.setOnValueChangedListener { _, _, newVal ->
        //Underscores for unused params
        //updatedOdometer = newVal
        //}

        submitButton.setOnClickListener {
            //Toast.makeText(applicationContext, "Press and hold to confirm")...
            //Do nothing actually
        }

        submitButton.setOnTouchListener(SubmitTouchListener(expandingSubmit))

        databaseReference = FirebaseDatabase.getInstance()
            .reference

    }

    inner class SubmitTouchListener(eE: TextView) : View.OnTouchListener
    {
        private var expandableElement: TextView
        private val outerExpandSpeed = 800L // ms
        private val outerShrinkSpeed = outerExpandSpeed / 3
        private val innerExpandSpeed = 1200L // ms
        private val innerShrinkSpeed = innerExpandSpeed / 3

        private var expandAnim: ScaleAnimation
        private var shrinkAnim: ScaleAnimation
        private var innerExpandAnim: ScaleAnimation
        private var innerShrinkAnim: ScaleAnimation
        //private var innerExpandAnim: ScaleAnimation
        //private var innerShrinkAnim: ScaleAnimation

        private var outerExpandStartTime: Long = 0
        private var innerExpandStartTime: Long = 0

        private val maxSize = 1.9f

        init
        {
            expandableElement = eE

            expandAnim = ScaleAnimation(1f, maxSize, 1f, maxSize, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            shrinkAnim = ScaleAnimation(maxSize, 1f, maxSize, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

            innerExpandAnim = ScaleAnimation(1f, maxSize, 1f, maxSize, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            innerShrinkAnim = ScaleAnimation(maxSize, 1f, maxSize, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

            expandAnim.interpolator = LinearInterpolator()
            shrinkAnim.interpolator = LinearInterpolator()
            innerExpandAnim.interpolator = LinearInterpolator()
            innerShrinkAnim.interpolator = LinearInterpolator()

            expandAnim.fillAfter = true
            shrinkAnim.fillAfter = true
            innerExpandAnim.fillAfter = true
            innerShrinkAnim.fillAfter = true

            expandAnim.duration = outerExpandSpeed
            shrinkAnim.duration = outerShrinkSpeed
            innerExpandAnim.duration = innerExpandSpeed
            innerShrinkAnim.duration = innerShrinkSpeed

            expandAnim.setAnimationListener(ExpandAnimListener())

            innerExpandAnim.setAnimationListener(InnerExpandAnimListener())

            innerShrinkAnim.setAnimationListener(InnerShrinkAnimListener())
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean
        {
            if (event?.action == MotionEvent.ACTION_DOWN)
            {
                // Start expanding expandingSubmit

                if (expandableElement.animation != null) expandableElement.animation.setAnimationListener(null)
                expandableElement.clearAnimation()

                if (submitButton.animation != null) submitButton.animation.setAnimationListener(null)
                submitButton.clearAnimation()

                expandAnim.setAnimationListener(ExpandAnimListener())
                innerExpandAnim.setAnimationListener(InnerExpandAnimListener())

                expandableElement.updateLayoutParams<ConstraintLayout.LayoutParams> { width = 128 * 3; height = 128 * 3 }
                submitButton.updateLayoutParams<ConstraintLayout.LayoutParams> { width = 128 * 3; height = 128 * 3 }

                outerExpandStartTime = 0
                innerExpandStartTime = 0

                expandableElement.startAnimation(expandAnim)
            }
            if (event?.action == MotionEvent.ACTION_UP)
            {
                Log.d("tap", "tap")

                if (innerExpandStartTime > 0) // If we lifted up when expanding the inner circle
                {
                    // Quickly shrink the expandingSubmit section

                    val timeHeld: Long = System.currentTimeMillis() - innerExpandStartTime

                    val completeRatio = (timeHeld / innerExpandSpeed.toDouble())
                    val newSize = 1 + (maxSize - 1) * completeRatio

                    innerShrinkAnim = ScaleAnimation(newSize.toFloat(), 1f, newSize.toFloat(), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    innerShrinkAnim.duration = innerShrinkSpeed
                    innerShrinkAnim.interpolator = LinearInterpolator()

                    innerShrinkAnim.setAnimationListener(InnerShrinkAnimListener())

                    innerExpandStartTime = 0
                    submitButton.startAnimation(innerShrinkAnim)
                }
                else
                {
                    // Quickly shrink the expandingSubmit section

                    val timeHeld: Long = System.currentTimeMillis() - outerExpandStartTime

                    val completeRatio = (timeHeld / outerExpandSpeed.toDouble())
                    val newSize = 1 + (maxSize - 1) * completeRatio
                    shrinkAnim = ScaleAnimation(newSize.toFloat(), 1f, newSize.toFloat(), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    shrinkAnim.duration = outerShrinkSpeed
                    shrinkAnim.interpolator = LinearInterpolator()

                    outerExpandStartTime = 0
                    expandableElement.startAnimation(shrinkAnim)
                }
            }

            return true
        }

        private inner class ExpandAnimListener : Animation.AnimationListener
        {
            override fun onAnimationRepeat(animation: Animation?)
            {
                // Do nothing
            }

            override fun onAnimationEnd(animation: Animation?)
            {
                if (System.currentTimeMillis() - outerExpandStartTime >= (outerExpandSpeed - 10))
                {
                    innerExpandStartTime = System.currentTimeMillis()
                    submitButton.startAnimation(innerExpandAnim)
                }
                outerExpandStartTime = 2.0.pow(62.0)
                    .toLong() // A Big number
            }

            override fun onAnimationStart(animation: Animation?)
            {
                outerExpandStartTime = System.currentTimeMillis()
            }
        }

        private inner class InnerShrinkAnimListener : Animation.AnimationListener
        {
            override fun onAnimationRepeat(animation: Animation?)
            {
                // Do nothing
            }

            override fun onAnimationEnd(animation: Animation?)
            {
                // Start shrinking the outer circle

                val newSize = maxSize
                shrinkAnim = ScaleAnimation(newSize, 1f, newSize, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                shrinkAnim.duration = outerShrinkSpeed
                shrinkAnim.interpolator = LinearInterpolator()

                innerExpandStartTime = 0
                expandableElement.startAnimation(shrinkAnim)
            }

            override fun onAnimationStart(animation: Animation?)
            {
                // Do nothing
            }
        }

        private inner class InnerExpandAnimListener : Animation.AnimationListener
        {
            override fun onAnimationRepeat(animation: Animation?)
            {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onAnimationEnd(animation: Animation?)
            {
                if (System.currentTimeMillis() - innerExpandStartTime >= (innerExpandSpeed - 10))
                {
                    Toast.makeText(expandableElement.context, "Submitting!", Toast.LENGTH_SHORT)
                        .show()

                    //Thread.sleep(500)
                    updatedOdometer = odometerPicker.value

                    updateOdometer(activity)
                }
                else
                {
                    // Quickly shrink the expandingSubmit section

                    val timeHeld: Long = System.currentTimeMillis() - outerExpandStartTime

                    val completeRatio = (timeHeld / outerExpandSpeed.toDouble())
                    val newSize = 1 + (maxSize - 1) * completeRatio
                    shrinkAnim = ScaleAnimation(newSize.toFloat(), 1f, newSize.toFloat(), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    shrinkAnim.duration = outerShrinkSpeed
                    shrinkAnim.interpolator = LinearInterpolator()

                    outerExpandStartTime = 0
                    expandableElement.startAnimation(shrinkAnim)
                }
            }

            override fun onAnimationStart(animation: Animation?)
            {
                innerExpandStartTime = System.currentTimeMillis()
            }
        }
    }

    fun updateOdometer(activity: Activity)
    {
        dataManager.setData("odometer", updatedOdometer, null)
    }
}