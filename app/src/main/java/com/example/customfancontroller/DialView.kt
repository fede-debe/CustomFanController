package com.example.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * add a top-level enum to represent the available fan speeds. Note that this enum is of type Int because
 * the values are string resources rather than actual strings.
 *
 * add an extension function next() that changes the current fan speed to the next speed in the list (from
 * OFF to LOW, MEDIUM, and HIGH, and then back to OFF).
 * */
private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when(this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}
/**
 * You'll use these as part of drawing the dial indicators and labels.
 * */
private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35
/**
 * Create a new Kotlin class called DialView. Modify the class definition to extend View. Click on View and then
 * click the red bulb. Choose Add Android View constructors using '@JvmOverloads'. Android Studio adds the
 * constructor from the View class. The @JvmOverloads annotation instructs the Kotlin compiler to generate
 * overloads for this function that substitute default parameter values.
 * */
class DialView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Define several variables you need in order to draw the custom view. The radius is the current radius of the circle.
     * This value is set when the view is drawn on the screen.  The fanSpeed is the current speed of the fan, which is one
     * of the values in the FanSpeed enumeration. By default that value is OFF. Finally pointPosition is an X,Y point that
     * will be used for drawing several of the view's elements on the screen. These values are created and initialized here
     * instead of when the view is actually drawn to ensure that the actual drawing step runs as fast as possible.
     * */
    private var radius = 0.0f // radius of circle
    private var fanSpeed = FanSpeed.OFF // the active selection
    private val pointPosition: PointF  = PointF(0.0f, 0.0f) // position var which will be used to draw label and indicator circle

    /**
     * In activity_main.xml, in the DialView, add attributes for fanColor1, fanColor2, and fanColor3, and set their values.
     * Use app: as the preface for the custom attribute (as in app:fanColor1) rather than android: because your custom
     * attributes belong to the schemas.android.com/apk/res/your_app_package_name namespace rather than the android
     * namespace.
     *
     * In order to use the attributes, you need to retrieve them. They are stored in an AttributeSet, that is handed to
     * your class upon creation, if it exists. You retrieve the attributes in init, and then you assign the attribute
     * values to local variables for caching. Declare variables to cache the attribute values.
     * */
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedMaxColor = 0

    /**
     * Inside the DialView class definition, initialize a Paint object with a handful of basic styles.
     * Import android.graphics.Paint and android.graphics.Typeface when requested. As with the variables,
     * these styles are initialized here to help speed up the drawing step.
     * */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    /**
     *  Add an init() block. Setting the view's isClickable property to true enables that view to accept user input.
     *
     *  Add the following code using the withStyledAttributes extension function. You supply the attributes and view,
     *  and and set your local variables.  Android and the Kotlin extension library (android-ktx) do a lot of work
     *  for you here! The android-ktx library provides Kotlin extensions with a strong quality-of-life focus. For
     *  example, the withStyledAttributes extension replaces a significant number of lines of rather tedious
     *  boilerplate code.
     *  */
    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }


    /**
     * The call to super.performClick() must happen first, which enables accessibility events as well as calls onClickListener().
     *
     * The next two lines increment the speed of the fan with the next() method, and set the view's content description to the
     * string resource representing the current speed (off, 1, 2 or 3).
     *
     * Finally, the invalidate() method invalidates the entire
     * view, forcing a call to onDraw() to redraw the view. If something in your custom view changes for any reason, including
     * user interaction, and the change needs to be displayed, call invalidate()
     * */
    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        invalidate()
        return true
    }

    /**
     * Override the onSizeChanged() method from the View class to calculate the size for the custom view's dial.
     * Import kotlin.math.min when requested.
     * */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    /**
     * add this code to define a computeXYForSpeed() extension function for the PointF class. Import kotlin.math.cos
     * and kotlin.math.sin when requested. This extension function on the PointF class calculates the X, Y coordinates
     * on the screen for the text label and current indicator (0, 1, 2, or 3), given the current FanSpeed position and
     * radius of the dial. You'll use this in onDraw().
     * */
    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    /**
     * Override the onDraw() method to render the view on the screen with the Canvas and Paint classes.
     *
     * drawCircle() -> This method uses the current view width and height to find the center of the circle,
     * the radius of the circle, and the current paint color. The width and height properties are members
     * of the View superclass and indicate the current dimensions of the view.
     *
     * markerRadius -> This part uses the PointF.computeXYforSpeed() extension method to calculate the X,Y
     * coordinates for the indicator center based on the current fan speed.
     *
     * labelRadius -> Finally, draw the fan speed labels (0, 1, 2, 3) at the appropriate positions around
     * the dial. This part of the method calls PointF.computeXYForSpeed() again to get the position for
     * each label, and reuses the pointPosition object each time to avoid allocations. Use drawText() to
     * draw the labels.
     * */
    override fun onDraw(canvas: Canvas) {
        // set the paint color to gray (Color.GRAY) or green (Color.GREEN) depending on whether the fan speed is OFF or any other value
        // paint.color = if (fanSpeed == FanSpeed.OFF) Color.GRAY else Color.GREEN

        /** Now you can use the local variables in your code. In onDraw() to set the dial color based on the current fan speed instead
         * of using the code above with only two colors.
         * */
        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedMaxColor
        }

        // Add this code to draw a circle for the dial, with the drawCircle() method.
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // Add this code to draw a smaller circle for the fan speed indicator mark, also with the drawCircle() method.
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius/12, paint)

        // draw the fan speed labels (0, 1, 2, 3) at the appropriate positions around the dial.
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }
}